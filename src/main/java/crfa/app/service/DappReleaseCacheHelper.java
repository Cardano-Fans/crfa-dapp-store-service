package crfa.app.service;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import crfa.app.repository.total.DappReleaseRepository;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import lombok.extern.slf4j.Slf4j;
import lombok.val;

import static crfa.app.domain.SortBy.SCRIPTS_INVOKED;
import static crfa.app.domain.SortOrder.ASC;

@Singleton
@Slf4j
public class DappReleaseCacheHelper {

    @Inject
    private DappReleaseRepository dappReleaseRepository;

    public Cache<String, Float> buildMaxReleaseVersionCache() {
        val releaseVersionsCache = CacheBuilder.newBuilder().<String, Float>build();

        dappReleaseRepository.listDappReleases(SCRIPTS_INVOKED, ASC).forEach(dAppRelease -> {
            val dappId = dAppRelease.getDappId();
            releaseVersionsCache.put(dappId, dappReleaseRepository.getMaxReleaseVersion(dappId));
        });

        return releaseVersionsCache;
    }

}
