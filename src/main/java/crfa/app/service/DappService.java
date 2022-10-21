package crfa.app.service;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import crfa.app.domain.*;
import crfa.app.repository.epoch.DappReleaseEpochRepository;
import crfa.app.repository.epoch.DappScriptsEpochRepository;
import crfa.app.repository.epoch.DappsEpochRepository;
import crfa.app.repository.total.DappReleaseRepository;
import crfa.app.domain.EpochLevelStats;
import crfa.app.utils.MoreOptionals;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import lombok.val;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static crfa.app.domain.SortBy.SCRIPTS_INVOKED;
import static crfa.app.domain.SortOrder.ASC;
import static crfa.app.utils.MoreMath.*;

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
                    .fees(epochGatherable.getFees())
                    .avgFee(epochGatherable.getAvgFee())
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
                val volumeDiff = safeSubtraction(nextStats.getVolume(), prevStats.getVolume());
                val feesDiff = safeSubtraction(nextStats.getFees(), prevStats.getFees());
                val avgFeesDiff = safeSubtraction(nextStats.getAvgFee(), prevStats.getAvgFee());
                val inflowsOutflowsDiff = safeSubtraction(nextStats.getInflowsOutflows(), prevStats.getInflowsOutflows());
                val uniqueAccountsDiff = safeSubtraction(nextStats.getUniqueAccounts(), prevStats.getUniqueAccounts());
                val trxCountDiff = safeSubtraction(nextStats.getTrxCount(), prevStats.getTrxCount());

                val volumeDiffPerc = safeMultiplication(safeDivision(volumeDiff, prevStats.getVolume()), (double) 100);
                val feesDiffPerc = safeMultiplication(safeDivision(feesDiff, prevStats.getFees()), (double) 100);
                val avgFeesDiffPerc = safeMultiplication(safeDivision(avgFeesDiff, prevStats.getAvgFee()), (double) 100);
                val inflowsOutflowsDiffPerc = safeMultiplication(safeDivision(inflowsOutflowsDiff, prevStats.getInflowsOutflows()), (double) 100);
                val uniqueAccountsDiffPerc = safeMultiplication(safeDivision(uniqueAccountsDiff, prevStats.getUniqueAccounts()), (double) 100);
                val trxCountDiffPerc = safeMultiplication(safeDivision(trxCountDiff, prevStats.getTrxCount()), (double) 100);

                val activityDiffPerc = Stream.of(volumeDiffPerc, uniqueAccountsDiffPerc, trxCountDiffPerc).filter(Objects::nonNull).mapToDouble(Double::doubleValue).average().getAsDouble();

                // TODO controversial but how to solve it better?
                if (Double.isInfinite(activityDiffPerc) || Double.isNaN(activityDiffPerc)) {
                    return null;
                }

                return new EpochDelta(
                        prevEpoch,
                        nextEpoch,
                        volumeDiff,
                        volumeDiffPerc,
                        feesDiff,
                        feesDiffPerc,
                        avgFeesDiff,
                        avgFeesDiffPerc,
                        inflowsOutflowsDiff,
                        inflowsOutflowsDiffPerc,
                        uniqueAccountsDiff,
                        uniqueAccountsDiffPerc,
                        trxCountDiff,
                        trxCountDiffPerc,
                        activityDiffPerc
                );
            } else {
                val trxCountDiff = safeSubtraction(nextStats.getTrxCount(), prevStats.getTrxCount());

                val trxCountDiffPerc = safeMultiplication(safeDivision(trxCountDiff, prevStats.getTrxCount()), (double) 100);

                val activityDiffPerc = Stream.of(trxCountDiffPerc).filter(Objects::nonNull).mapToDouble(Double::doubleValue).average().getAsDouble();

                return EpochDelta.builder()
                        .trxCountDiff(trxCountDiff)
                        .trxCountDiffPerc(trxCountDiffPerc)
                        .activityDiffPerc(activityDiffPerc)
                        .build();
            }
        });

        return epochDelta == null ? Optional.empty() : epochDelta;
    }

    public int currentEpoch() {
        return scrollsOnChainDataService.currentEpoch().orElseThrow();
    }

}
