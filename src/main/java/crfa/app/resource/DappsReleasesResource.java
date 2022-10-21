package crfa.app.resource;

import crfa.app.domain.SortBy;
import crfa.app.domain.SortOrder;
import crfa.app.repository.total.DappReleaseRepository;
import crfa.app.resource.model.DappReleaseResult;
import crfa.app.service.DappService;
import io.micronaut.http.annotation.Controller;
import io.micronaut.http.annotation.Get;
import io.micronaut.http.annotation.QueryValue;
import jakarta.inject.Inject;
import lombok.extern.slf4j.Slf4j;
import lombok.val;

import java.util.List;
import java.util.Optional;

import static crfa.app.domain.SortBy.RELEASE_NUMBER;
import static crfa.app.domain.SortBy.SCRIPTS_INVOKED;
import static crfa.app.domain.SortOrder.ASC;
import static crfa.app.utils.MoreMath.safeDivision;

@Controller("/dapps")
@Slf4j
public class DappsReleasesResource {

    @Inject
    private DappReleaseRepository dappReleaseRepository;

    @Inject
    private DappService dappService;

    @Deprecated
    @Get(uri = "/list-releases", produces = "application/json")
    public List<DappReleaseResult> listDappReleases(@QueryValue Optional<SortBy> sortBy,
                                                    @QueryValue Optional<SortOrder> sortOrder) {
        val releaseVersionsCache = dappService.buildMaxReleaseVersionCache();

        return dappReleaseRepository.listDappReleases(sortBy.orElse(SCRIPTS_INVOKED), sortOrder.orElse(SortOrder.DESC))
                .stream()
                .map(dAppRelease -> {
                    val maxReleaseVersion = releaseVersionsCache.getIfPresent(dAppRelease.getDappId());

                    val isLastVersion = dAppRelease.isLatestVersion(maxReleaseVersion);

                    val dappType = dAppRelease.getDAppType();

                    return DappReleaseResult.builder()
                            .id(dAppRelease.getDappId())
                            .category(dAppRelease.getCategory())
                            .subCategory(dAppRelease.getSubCategory())
                            .dAppType(dappType)
                            .fullName(dAppRelease.getFullName())
                            .icon(dAppRelease.getIcon())
                            .key(dAppRelease.getId())
                            .link(dAppRelease.getLink())
                            .releaseName(dAppRelease.getReleaseName())
                            .name(dAppRelease.getName())
                            .twitter(dAppRelease.getTwitter())
                            .updateTime(dAppRelease.getUpdateTime())
                            .releaseNumber(dAppRelease.getReleaseNumber())
                            .fees(dAppRelease.getFees())
                            .avgFee(safeDivision(dAppRelease.getFees(), dAppRelease.getScriptInvocationsCount()))
                            .scriptsLocked(dAppRelease.getScriptsLocked())
                            .transactionsCount(dAppRelease.getScriptInvocationsCount())
                            .trxCount(dAppRelease.getScriptInvocationsCount())
                            .latestVersion(isLastVersion)
                            .uniqueAccounts(dAppRelease.getUniqueAccounts())
                            .volume(dAppRelease.getVolume())
                            .contractOpenSourcedLink(dAppRelease.getContractLink())
                            .contractsAuditedLink(dAppRelease.getAuditLink())
                            .epochLevelData(dappService.getAllEpochLevelData(dAppRelease, false))
                            .build();
                }).toList();
    }

    @Get(uri = "/find-release/{id}", produces = "application/json")
    public List<DappReleaseResult> findDappRelease(String id) {
        return listDappReleases(Optional.of(RELEASE_NUMBER), Optional.of(ASC))
                .stream()
                .filter(dappReleaseResult -> dappReleaseResult.getId().equalsIgnoreCase(id))
                .toList();
    }

}
