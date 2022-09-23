package crfa.app.service;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import crfa.app.repository.DappReleaseRepository;
import crfa.app.resource.InvalidParameterException;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import lombok.val;

import java.util.Optional;

import static crfa.app.domain.SortBy.SCRIPTS_INVOKED;
import static crfa.app.domain.SortOrder.ASC;

@Singleton
public class DappService {

    @Inject
    private DappReleaseRepository dappReleaseRepository;

    public Cache<String, Float> buildMaxReleaseVersionCache() {
        Cache<String, Float> releaseVersionsCache = CacheBuilder.newBuilder()
                .build();

        try {
            dappReleaseRepository.listDappReleases(Optional.of(SCRIPTS_INVOKED), Optional.of(ASC)).forEach(dAppRelease -> {
                val dappId = dAppRelease.getId();
                releaseVersionsCache.put(dappId, dappReleaseRepository.getMaxReleaseVersion(dappId));
            });
        } catch (InvalidParameterException e) {
            throw new RuntimeException(e);
        }

        return releaseVersionsCache;
    }

}
