package crfa.app.service;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import crfa.app.domain.*;
import crfa.app.repository.epoch.DappReleaseEpochRepository;
import crfa.app.repository.epoch.DappScriptsEpochRepository;
import crfa.app.repository.epoch.DappsEpochRepository;
import crfa.app.repository.total.DappReleaseRepository;
import crfa.app.resource.model.EpochLevelStats;
import crfa.app.utils.MoreOptionals;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import lombok.val;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static crfa.app.domain.SortBy.SCRIPTS_INVOKED;
import static crfa.app.domain.SortOrder.ASC;
import static crfa.app.utils.MoreInts.nullSafe;

@Singleton
public class DappService {

    @Inject
    private DappReleaseRepository dappReleaseRepository;

    @Inject
    private ScrollsOnChainDataService scrollsOnChainDataService;

    @Inject
    private DappsEpochRepository dappsEpochRepository;

    @Inject
    private DappReleaseEpochRepository dappReleaseEpochRepository;

    @Inject
    private DappScriptsEpochRepository dappScriptsEpochRepository;

    public Cache<String, Float> buildMaxReleaseVersionCache() {
        val releaseVersionsCache = CacheBuilder.newBuilder().<String, Float>build();

        dappReleaseRepository.listDappReleases(SCRIPTS_INVOKED, ASC).forEach(dAppRelease -> {
            val dappId = dAppRelease.getDappId();
            releaseVersionsCache.put(dappId, dappReleaseRepository.getMaxReleaseVersion(dappId));
        });

        return releaseVersionsCache;
    }

    public Map<Integer, EpochLevelStats> gatherEpochLevelData(Collection<? extends EpochGatherable> it) {
        val epochLevelStats = new HashMap<Integer, EpochLevelStats>();

        it.forEach(epochGatherable -> {
            val epochNo = epochGatherable.getEpochNo();

            epochLevelStats.put(epochNo, EpochLevelStats.builder()
                    .volume(epochGatherable.getVolume())
                    .inflowsOutflows(epochGatherable.getInflowsOutflows())
                    .uniqueAccounts(epochGatherable.getUniqueAccounts())
                    .trxCount(epochGatherable.getScriptInvocationsCount())
                    .closed(epochGatherable.isClosedEpoch())
                    .build()
            );
        });

        return epochLevelStats;
    }

    public Optional<EpochLevelData> getAllEpochLevelData(DApp dapp, boolean includeEpochDetails) {
        val id = dapp.getId();
        var dappEpochList = dappsEpochRepository.findByDappId(id);
        val epochData = gatherEpochLevelData(dappEpochList);

        val hasSpend = dapp.getDAppType().hasSpend();

        val one = getLastClosedEpochsDelta(epochData, 1, hasSpend); // 1 epoch
        val six = getLastClosedEpochsDelta(epochData, 6, hasSpend); // 1 month
        val eighten = getLastClosedEpochsDelta(epochData, (3 * 6), hasSpend); // 3 months

        val b = EpochLevelData.builder()
                .epochData(Optional.empty())
                .lastEpochDeltaWithOnlyClosedEpochs(one)
                .lastMonthDeltaWithOnlyClosedEpochs(six)
                .lastQuarterDeltaWithOnlyClosedEpochs(eighten);

        if (includeEpochDetails) {
            b.epochData(Optional.of(epochData));
        }

        return Optional.of(b.build());
    }

    public Optional<EpochLevelData> getAllEpochLevelData(DAppRelease dAppRelease, boolean includeEpochLevel) {
        val releaseKey = dAppRelease.getId();
        val epochData = gatherEpochLevelData(dappReleaseEpochRepository.findByReleaseKey(releaseKey));

        val hasSpend = dAppRelease.getDAppType().hasSpend();

        val one = getLastClosedEpochsDelta(epochData, 1, hasSpend); // 1 epoch
        val six = getLastClosedEpochsDelta(epochData, 6, hasSpend); // 1 month
        val eighten = getLastClosedEpochsDelta(epochData, (3 * 6), hasSpend); // 3 months

        val b = EpochLevelData.builder()
                .epochData(Optional.empty())
                .lastEpochDeltaWithOnlyClosedEpochs(one)
                .lastMonthDeltaWithOnlyClosedEpochs(six)
                .lastQuarterDeltaWithOnlyClosedEpochs(eighten);

        if (includeEpochLevel) {
            b.epochData(Optional.of(epochData));
        }

        return Optional.of(b.build());
    }

    public Optional<EpochLevelData> getAllEpochLevelData(DappScriptItem dappScriptItem, boolean includeEpochLevel) {
        val hash = dappScriptItem.getHash();
        val dappScriptItemEpoches = dappScriptsEpochRepository.listByHash(hash);
        val epochData = gatherEpochLevelData(dappScriptItemEpoches);

        val hasSpend = dappScriptItem.getScriptType() == ScriptType.SPEND;

        val one = getLastClosedEpochsDelta(epochData, 1, hasSpend); // 1 epoch
        val six = getLastClosedEpochsDelta(epochData, 6, hasSpend); // 1 month
        val eighten = getLastClosedEpochsDelta(epochData, (3 * 6), hasSpend); // 3 months

        val b = EpochLevelData.builder()
                .epochData(Optional.empty())
                .lastEpochDeltaWithOnlyClosedEpochs(one)
                .lastMonthDeltaWithOnlyClosedEpochs(six)
                .lastQuarterDeltaWithOnlyClosedEpochs(eighten);

        if (includeEpochLevel) {
            b.epochData(Optional.of(epochData));
        }

        return Optional.of(b.build());
    }

    public Optional<EpochDelta> getLastClosedEpochsDelta(Map<Integer, EpochLevelStats> stats, int epochGap, boolean hasSpend) {
        val closedEpochsMap = stats.entrySet().stream().filter(entry -> entry.getValue().isClosed()).collect(Collectors.toMap(
                Map.Entry::getKey,
                Map.Entry::getValue
        ));

        val nextEpochM = closedEpochsMap.keySet().stream().max(Integer::compare);
        val prevEpochM = nextEpochM.map(lastEpochNo -> lastEpochNo - epochGap);

        var epochDelta = MoreOptionals.allOf(prevEpochM, nextEpochM, (prevEpoch, nextEpoch) -> {
            val prevStats = closedEpochsMap.get(prevEpoch); // prev
            val nextStats = closedEpochsMap.get(nextEpoch); // next

            if (prevStats == null || nextEpoch == null) {
                return null;
            }

            if (hasSpend) {
                val volumeDiff = nullSafe(nextStats.getVolume()) - nullSafe(prevStats.getVolume());
                val inflowsOutflowsDiff = nullSafe(nextStats.getInflowsOutflows()) - nullSafe(prevStats.getInflowsOutflows());
                val uniqueAccountsDiff = nullSafe(nextStats.getUniqueAccounts()) - nullSafe(prevStats.getUniqueAccounts());
                val trxCountDiff = nullSafe(nextStats.getTrxCount()) - nullSafe(prevStats.getTrxCount());

                val volumeDiffPerc = (float) volumeDiff / nullSafe(prevStats.getVolume()) * 100;
                val inflowsOutflowsDiffPerc = (float) inflowsOutflowsDiff / nullSafe(prevStats.getInflowsOutflows()) * 100;
                val uniqueAccountsDiffPerc = (float) uniqueAccountsDiff / nullSafe(prevStats.getUniqueAccounts()) * 100;
                val trxCountDiffPerc = (float) trxCountDiff / nullSafe(prevStats.getTrxCount()) * 100;

                val activityDiffPerc = Stream.of(volumeDiffPerc, uniqueAccountsDiffPerc, trxCountDiffPerc).mapToDouble(Float::doubleValue).average().getAsDouble();

                // TODO controversial but how to solve it better?
                if (Double.isInfinite(activityDiffPerc) || Double.isNaN(activityDiffPerc)) {
                    return null;
                }

                return new EpochDelta(
                        prevEpoch,
                        nextEpoch,
                        volumeDiff,
                        Double.isNaN(volumeDiffPerc) | Double.isInfinite(volumeDiffPerc) ? 0 : volumeDiffPerc,
                        inflowsOutflowsDiff,
                        Double.isNaN(inflowsOutflowsDiffPerc) || Double.isInfinite(inflowsOutflowsDiffPerc) ? 0 : inflowsOutflowsDiffPerc,
                        uniqueAccountsDiff,
                        Double.isNaN(uniqueAccountsDiffPerc) || Double.isInfinite(uniqueAccountsDiffPerc) ? 0 : uniqueAccountsDiffPerc,
                        trxCountDiff,
                        Double.isNaN(trxCountDiffPerc) || Double.isInfinite(trxCountDiffPerc) ? 0 : trxCountDiffPerc,
                        Double.isNaN(activityDiffPerc) || Double.isInfinite(activityDiffPerc) ? 0 : activityDiffPerc
                );
            } else {
                val trxCountDiff = nullSafe(nextStats.getTrxCount()) - nullSafe(prevStats.getTrxCount());

                val trxCountDiffPerc = (float) trxCountDiff / nullSafe(prevStats.getTrxCount()) * 100;

                val activityDiffPerc = Stream.of(trxCountDiffPerc).mapToDouble(Float::doubleValue).average().getAsDouble();

                return EpochDelta.builder()
                        .trxCountDiff(trxCountDiff)
                        .trxCountDiffPerc(Double.isNaN(trxCountDiffPerc) || Double.isInfinite(trxCountDiffPerc) ? 0 : trxCountDiffPerc)
                        .activityDiffPerc(Double.isNaN(activityDiffPerc) || Double.isInfinite(activityDiffPerc) ? 0 : activityDiffPerc)
                        .build();
            }
        });

        return epochDelta == null ? Optional.empty() : epochDelta;
    }

    public int currentEpoch() {
        return scrollsOnChainDataService.currentEpoch().orElseThrow();
    }

}
