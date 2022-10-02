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

import static crfa.app.domain.SortBy.SCRIPTS_INVOKED;
import static crfa.app.domain.SortOrder.ASC;
import static crfa.app.utils.MoreInts.nullSafe;

@Singleton
public class DappService {

    @Inject
    private DappReleaseRepository dappReleaseRepository;

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

    public Optional<EpochDelta> getLastClosedEpochsDelta(Map<Integer, EpochLevelStats> stats) {
        val closedEpochsMap = stats.entrySet().stream().filter(entry -> entry.getValue().isClosed()).collect(Collectors.toMap(
                Map.Entry::getKey,
                Map.Entry::getValue
        ));

        val nextEpochM = closedEpochsMap.keySet().stream().max(Integer::compare);
        val prevEpochM = nextEpochM.map(lastEpochNo -> lastEpochNo - 1);

        return MoreOptionals.allOf(prevEpochM, nextEpochM, (prevEpoch, nextEpoch) -> {
            val prevStats = closedEpochsMap.get(prevEpoch); // prev
            val nextStats = closedEpochsMap.get(nextEpoch); // next

            val volumeDiff = nullSafe(nextStats.getVolume()) - nullSafe(prevStats.getVolume());
            val inflowsOutflowsDiff = nullSafe(nextStats.getInflowsOutflows()) - nullSafe(prevStats.getInflowsOutflows());
            val uniqueAccountsDiff = nullSafe(nextStats.getUniqueAccounts()) - nullSafe(prevStats.getUniqueAccounts());
            val trxCountDiff = nullSafe(nextStats.getTrxCount()) - nullSafe(prevStats.getTrxCount());

            var volumeDiffPerc = (float) volumeDiff / nullSafe(prevStats.getVolume());
            var inflowsOutflowsDiffPerc = (float) inflowsOutflowsDiff / nullSafe(prevStats.getInflowsOutflows());
            var uniqueAccountsDiffPerc = (float) uniqueAccountsDiff / nullSafe(prevStats.getUniqueAccounts());
            var trxCountDiffPerc = (float) trxCountDiff / nullSafe(prevStats.getTrxCount());

            return new EpochDelta(
                    prevEpoch,
                    nextEpoch,
                    volumeDiff,
                    volumeDiffPerc,
                    inflowsOutflowsDiff,
                    inflowsOutflowsDiffPerc,
                    uniqueAccountsDiff,
                    uniqueAccountsDiffPerc,
                    trxCountDiff,
                    trxCountDiffPerc
            );
        });
    }

}
