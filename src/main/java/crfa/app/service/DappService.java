package crfa.app.service;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import crfa.app.domain.EpochDelta;
import crfa.app.domain.EpochGatherable;
import crfa.app.repository.total.DappReleaseRepository;
import crfa.app.resource.InvalidParameterException;
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

    public Cache<String, Float> buildMaxReleaseVersionCache() {
        val releaseVersionsCache = CacheBuilder.newBuilder().<String, Float>build();

        try {
            dappReleaseRepository.listDappReleases(Optional.of(SCRIPTS_INVOKED), Optional.of(ASC)).forEach(dAppRelease -> {
                val dappId = dAppRelease.getDappId();
                releaseVersionsCache.put(dappId, dappReleaseRepository.getMaxReleaseVersion(dappId));
            });
        } catch (InvalidParameterException e) {
            throw new RuntimeException(e);
        }

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

    public Optional<EpochDelta> getLastClosedEpochsDelta(Map<Integer, EpochLevelStats> stats, int epochGap) {
        val closedEpochsMap = stats.entrySet().stream().filter(entry -> entry.getValue().isClosed()).collect(Collectors.toMap(
                Map.Entry::getKey,
                Map.Entry::getValue
        ));

        val nextEpochM = closedEpochsMap.keySet().stream().max(Integer::compare);
        val prevEpochM = nextEpochM.map(lastEpochNo -> lastEpochNo - epochGap);

        return MoreOptionals.allOf(prevEpochM, nextEpochM, (prevEpoch, nextEpoch) -> {
            val prevStats = closedEpochsMap.get(prevEpoch); // prev
            val nextStats = closedEpochsMap.get(nextEpoch); // next

            val volumeDiff = nullSafe(nextStats.getVolume()) - nullSafe(prevStats.getVolume());
            val inflowsOutflowsDiff = nullSafe(nextStats.getInflowsOutflows()) - nullSafe(prevStats.getInflowsOutflows());
            val uniqueAccountsDiff = nullSafe(nextStats.getUniqueAccounts()) - nullSafe(prevStats.getUniqueAccounts());
            val trxCountDiff = nullSafe(nextStats.getTrxCount()) - nullSafe(prevStats.getTrxCount());

            val volumeDiffPerc = (float) volumeDiff / nullSafe(prevStats.getVolume()) * 100;
            val inflowsOutflowsDiffPerc = (float) inflowsOutflowsDiff / nullSafe(prevStats.getInflowsOutflows()) * 100;
            val uniqueAccountsDiffPerc = (float) uniqueAccountsDiff / nullSafe(prevStats.getUniqueAccounts()) * 100;
            val trxCountDiffPerc = (float) trxCountDiff / nullSafe(prevStats.getTrxCount()) * 100;

            val activityDiffPerc = Stream.of(volumeDiffPerc, uniqueAccountsDiffPerc, trxCountDiffPerc).mapToDouble(Float::doubleValue).average().getAsDouble();

            return new EpochDelta(
                    prevEpoch,
                    nextEpoch,
                    volumeDiff,
                    Double.isNaN(volumeDiffPerc) | Double.isInfinite(volumeDiffPerc)  ? 0 : volumeDiffPerc,
                    inflowsOutflowsDiff,
                    Double.isNaN(inflowsOutflowsDiffPerc) || Double.isInfinite(inflowsOutflowsDiffPerc) ? 0 : inflowsOutflowsDiffPerc,
                    uniqueAccountsDiff,
                    Double.isNaN(uniqueAccountsDiffPerc) || Double.isInfinite(uniqueAccountsDiffPerc) ? 0 : uniqueAccountsDiffPerc,
                    trxCountDiff,
                    Double.isNaN(trxCountDiffPerc) || Double.isInfinite(trxCountDiffPerc) ? 0 : trxCountDiffPerc,
                    Double.isNaN(activityDiffPerc) || Double.isInfinite(activityDiffPerc) ? 0 : activityDiffPerc
            );
        });
    }

    public int currentEpoch() {
        return scrollsOnChainDataService.currentEpoch().orElseThrow().intValue();
    }

}
