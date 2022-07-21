package crfa.app.service;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import crfa.app.domain.DAppRelease;
import crfa.app.domain.SortBy;
import crfa.app.domain.SortOrder;
import crfa.app.repository.DappReleasesRepository;
import crfa.app.resource.InvalidParameterException;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.TreeSet;

import static crfa.app.domain.SortBy.SCRIPTS_INVOKED;
import static crfa.app.domain.SortOrder.ASC;
import static java.util.Comparator.comparingInt;
import static java.util.stream.Collectors.collectingAndThen;
import static java.util.stream.Collectors.toCollection;

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

    public List<DAppRelease> dappUniqueReleases() {
        try {
            return dappReleasesRepository.listDappReleases(Optional.of(SCRIPTS_INVOKED), Optional.of(ASC))
                    .stream()
                    .collect(collectingAndThen(toCollection(() -> new TreeSet<>(comparingInt(d -> d.getId().hashCode()))),
                            ArrayList::new));
        } catch (InvalidParameterException e) {
            throw new RuntimeException("should never happen", e);
        }
    }

}
