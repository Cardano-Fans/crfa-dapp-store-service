package crfa.app.resource;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import crfa.app.domain.SortBy;
import crfa.app.domain.SortOrder;
import crfa.app.repository.DappReleaseItemRepository;
import crfa.app.repository.DappReleasesRepository;
import io.micronaut.http.annotation.Controller;
import io.micronaut.http.annotation.Get;
import io.micronaut.http.annotation.PathVariable;
import io.micronaut.http.annotation.QueryValue;
import jakarta.inject.Inject;
import lombok.extern.slf4j.Slf4j;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;

@Controller("/dapps")
@Slf4j
public class DappsResource {

    @Inject
    private DappReleasesRepository dappReleasesRepository;

    @Inject
    private DappReleaseItemRepository dappReleaseItemRepository;

    @Get(uri = "/list-releases", produces = "application/json")
    public List<DappReleaseResult> listDappReleases(@QueryValue Optional<SortBy> sortBy,
                                                    @QueryValue Optional<SortOrder> sortOrder) throws InvalidParameterException {

        Cache<String, Float> releaseVersionsCache = CacheBuilder.newBuilder()
                .build();

        return dappReleasesRepository.listDappReleases(sortBy, sortOrder)
                .stream().map(dAppRelease -> {
                    var maxReleaseVersion = retrieveMaxReleaseVersion(releaseVersionsCache, dAppRelease.getId());

                    return DappReleaseResult.builder()
                            .scriptInvocationsCount(dAppRelease.getScriptInvocationsCount())
                            .contractOpenSource(dAppRelease.getContractOpenSource())
                            .contractAudited(dAppRelease.getAuditLink() != null)
                            .category(dAppRelease.getCategory())
                            .subCategory(dAppRelease.getSubCategory())
                            .dAppType(dAppRelease.getDAppType())
                            .fullName(dAppRelease.getFullName())
                            .id(dAppRelease.getId())
                            .icon(dAppRelease.getIcon())
                            .key(dAppRelease.getKey())
                            .link(dAppRelease.getLink())
                            .releaseName(dAppRelease.getReleaseName())
                            .name(dAppRelease.getName())
                            .twitter(dAppRelease.getTwitter())
                            .updateTime(dAppRelease.getUpdateTime())
                            .releaseNumber(dAppRelease.getReleaseNumber())
                            .transactionsCount(dAppRelease.getTransactionsCount())
                            .scriptsLocked(dAppRelease.getScriptsLocked())
                            .latestVersion(dAppRelease.isLatestVersion(maxReleaseVersion))
                            .build();
                }).collect(Collectors.toList());
    }

    @Get(uri = "/by-release-key/{releaseKey}", produces = "application/json")
    public DappScriptsResponse listScriptsResponse(@PathVariable String releaseKey,
                                                   @QueryValue Optional<SortBy> sortBy,
                                                   @QueryValue Optional<SortOrder> sortOrder) throws DappReleaseNotFoundException, InvalidParameterException {
        var maybeDappRelease = dappReleasesRepository.findByReleaseKey(releaseKey);

        if (maybeDappRelease.isEmpty()) {
            throw new DappReleaseNotFoundException("Dapp release key not found: " + releaseKey);
        }

        var dappRelease = maybeDappRelease.get();

        var releaseItems = dappReleaseItemRepository.listReleaseItems(releaseKey, sortBy, sortOrder);

        return DappScriptsResponse.builder()
                .release(dappRelease)
                .scripts(releaseItems)
                .build();
    }

    private float retrieveMaxReleaseVersion(Cache<String, Float> releaseVersionsCache, String dappId) {
        try {
            return releaseVersionsCache.get(dappId, () -> dappReleasesRepository.getMaxReleaseVersion(dappId));
        } catch (ExecutionException e) {
            throw new RuntimeException(e);
        }
    }

}
