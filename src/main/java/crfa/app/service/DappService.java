package crfa.app.service;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import crfa.app.domain.SortBy;
import crfa.app.domain.SortOrder;
import crfa.app.repository.DappReleasesRepository;
import crfa.app.resource.InvalidParameterException;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;

import java.util.Optional;

@Singleton
public class DappService {

    @Inject
    private DappReleasesRepository dappReleasesRepository;

    public Cache<String, Float> buildMaxReleaseVersionCache() {
        Cache<String, Float> releaseVersionsCache = CacheBuilder.newBuilder()
                .build();

        try {
            dappReleasesRepository.listDappReleases(Optional.of(SortBy.SCRIPTS_INVOKED), Optional.of(SortOrder.ASC)).forEach(dAppRelease -> {
                        var dappId = dAppRelease.getId();
                        releaseVersionsCache.put(dappId, dappReleasesRepository.getMaxReleaseVersion(dappId));
                    });
        } catch (InvalidParameterException e) {
            throw new RuntimeException(e);
        }

        return releaseVersionsCache;
    }

}
