package crfa.app.service;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import crfa.app.domain.EpochGatherable;
import crfa.app.repository.total.DappReleaseRepository;
import crfa.app.resource.InvalidParameterException;
import crfa.app.resource.model.EpochLevelStats;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import lombok.val;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import static crfa.app.domain.SortBy.SCRIPTS_INVOKED;
import static crfa.app.domain.SortOrder.ASC;

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

        it.forEach(dappScriptItemEpoch -> {
            val epochNo = dappScriptItemEpoch.getEpochNo();

            epochLevelStats.put(epochNo, EpochLevelStats.builder()
                    .volume(dappScriptItemEpoch.getVolume())
                    .inflowsOutflows(dappScriptItemEpoch.getInflowsOutflows())
                    .uniqueAccounts(dappScriptItemEpoch.getUniqueAccounts())
                    .trxCount(dappScriptItemEpoch.getScriptInvocationsCount())
                    .build()
            );
        });

        return epochLevelStats;
    }

}
