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

import static crfa.app.domain.SortBy.SCRIPTS_INVOKED;
import static crfa.app.domain.SortOrder.ASC;

@Singleton
public class DappService {

    @Inject
    private DappReleasesRepository dappReleasesRepository;

    public Cache<String, Float> buildMaxReleaseVersionCache() {
        Cache<String, Float> releaseVersionsCache = CacheBuilder.newBuilder()
                .build();

        try {
            dappReleasesRepository.listDappReleases(Optional.of(SCRIPTS_INVOKED), Optional.of(ASC)).forEach(dAppRelease -> {
                var dappId = dAppRelease.getId();
                releaseVersionsCache.put(dappId, dappReleasesRepository.getMaxReleaseVersion(dappId));
            });
        } catch (InvalidParameterException e) {
            throw new RuntimeException(e);
        }

        return releaseVersionsCache;
    }

}
