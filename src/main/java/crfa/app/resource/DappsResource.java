package crfa.app.resource;

import crfa.app.domain.DappAggrType;
import crfa.app.domain.ScriptType;
import crfa.app.domain.SortBy;
import crfa.app.domain.SortOrder;
import crfa.app.repository.DappReleaseItemRepository;
import crfa.app.repository.DappReleasesRepository;
import crfa.app.repository.DappsRepository;
import crfa.app.service.DappService;
import io.micronaut.http.annotation.Controller;
import io.micronaut.http.annotation.Get;
import io.micronaut.http.annotation.PathVariable;
import io.micronaut.http.annotation.QueryValue;
import jakarta.inject.Inject;
import lombok.extern.slf4j.Slf4j;
import lombok.val;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static crfa.app.domain.DappAggrType.LAST;
import static crfa.app.domain.SortBy.RELEASE_NUMBER;
import static crfa.app.domain.SortOrder.ASC;

@Controller("/dapps")
@Slf4j
public class DappsResource {

    @Inject
    private DappsRepository dappsRepository;

    @Inject
    private DappReleasesRepository dappReleasesRepository;

    @Inject
    private DappReleaseItemRepository dappReleaseItemRepository;

    @Inject
    private DappService dappService;

    @Get(uri = "/list-releases", produces = "application/json")
    public List<DappReleaseResult> listDappReleases(@QueryValue Optional<SortBy> sortBy,
                                                    @QueryValue Optional<SortOrder> sortOrder) throws InvalidParameterException {

        var releaseVersionsCache = dappService.buildMaxReleaseVersionCache();

        return dappReleasesRepository.listDappReleases(sortBy, sortOrder)
                .stream().map(dAppRelease -> {
                    var maxReleaseVersion = releaseVersionsCache.getIfPresent(dAppRelease.getId());

                    final var isLastVersion = dAppRelease.isLatestVersion(maxReleaseVersion);

                    var b = DappReleaseResult.builder()
                            .scriptInvocationsCount(dAppRelease.getScriptInvocationsCount())
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
                            .trxCount(dAppRelease.getScriptInvocationsCount())
                            .latestVersion(isLastVersion);

                    b.contractOpenSourcedLink(dAppRelease.getContractLink());
                    b.contractsAuditedLink(dAppRelease.getAuditLink());

                    return b.build();
                }).toList();
    }

    @Get(uri = "/find-release/{id}", produces = "application/json")
    public List<DappReleaseResult> findDappRelease(String id) {
        try {
            return listDappReleases(Optional.of(RELEASE_NUMBER), Optional.of(ASC))
                    .stream()
                    .filter(dappReleaseResult -> dappReleaseResult.getId().equalsIgnoreCase(id))
                    .collect(Collectors.toList());
        } catch (InvalidParameterException e) {
            throw new RuntimeException(e);
        }
    }

    @Get(uri = "/list-dapps", produces = "application/json")
    public List<DappResult> listDapps(@QueryValue Optional<SortBy> sortBy,
                                      @QueryValue Optional<SortOrder> sortOrder,
                                      @QueryValue Optional<DappAggrType> dappAggrType) throws InvalidParameterException {

        var dappAggrTypeWithFallback = dappAggrType.orElse(DappAggrType.def());

        return dappsRepository.listDapps(sortBy, sortOrder, dappAggrTypeWithFallback)
                .stream().map(dapp -> {
                    val transactionsCount = dappAggrTypeWithFallback == LAST ? dapp.getLastVersionTransactionsCount() : dapp.getTransactionsCount();
                    val scriptInvocationsCount = dappAggrTypeWithFallback == LAST ? dapp.getLastVersionScriptInvocationsCount() : dapp.getScriptInvocationsCount();
                    val scriptsLocked = dappAggrTypeWithFallback == LAST ? dapp.getLastVersionScriptsLocked() : dapp.getScriptsLocked();

                    return DappResult.builder()
                            .category(dapp.getCategory())
                            .subCategory(dapp.getSubCategory())
                            .dAppType(dapp.getDAppType())
                            .id(dapp.getId())
                            .icon(dapp.getIcon())
                            .link(dapp.getLink())
                            .name(dapp.getName())
                            .twitter(dapp.getTwitter())
                            .transactionsCount(transactionsCount)
                            .scriptsLocked(scriptsLocked)
                            .scriptInvocationsCount(scriptInvocationsCount)
                            .lastVersionContractsOpenSourced(dapp.getLastVersionOpenSourceLink() != null)
                            .lastVersionContractsAudited(dapp.getLastVersionAuditLink() != null)
                            .lastVersionContractsOpenSourcedLink(dapp.getLastVersionOpenSourceLink())
                            .lastVersionContractsAuditedLink(dapp.getLastVersionAuditLink())
                            .trxCount(scriptInvocationsCount)
                            .updateTime(dapp.getUpdateTime())
                            .build();
                }).toList();
    }

    @Get(uri = "/by-release-key/{releaseKey}", produces = "application/json")
    public DappScriptsResponse listScriptsResponse(@PathVariable String releaseKey,
                                                   @QueryValue Optional<SortBy> sortBy,
                                                   @QueryValue Optional<SortOrder> sortOrder) throws DappReleaseNotFoundException, InvalidParameterException {
        var maybeDappRelease = dappReleasesRepository.findByReleaseKey(releaseKey);

        if (maybeDappRelease.isEmpty()) {
            throw new DappReleaseNotFoundException("Dapp release key not found: " + releaseKey);
        }

        var releaseVersionsCache = dappService.buildMaxReleaseVersionCache();

        var dAppRelease = maybeDappRelease.get();

        val scriptInvocationsCount = dAppRelease.getScriptInvocationsCount();
        val transactionsCount = dAppRelease.getTransactionsCount();

        val maxReleaseVersion = releaseVersionsCache.getIfPresent(dAppRelease.getId());

        val isLastVersion = dAppRelease.isLatestVersion(maxReleaseVersion);

        var b = DappReleaseResult.builder()
                .scriptInvocationsCount(scriptInvocationsCount)
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
                .transactionsCount(transactionsCount)
                .scriptsLocked(dAppRelease.getScriptsLocked())
                .trxCount(scriptInvocationsCount)
                .latestVersion(isLastVersion);

        b.contractOpenSourcedLink(dAppRelease.getContractLink());
        b.contractsAuditedLink(dAppRelease.getAuditLink());

        var releaseItems = dappReleaseItemRepository.listReleaseItemsByReleaseKey(releaseKey, sortBy, sortOrder)
                .stream()
                .map(d -> {
                    return DAppReleaseItemResult.builder()
                    .dappId(d.getDappId())
                    .hash(d.getHash())
                    .scriptsLocked(d.getScriptsLocked())
                    .scriptType(d.getScriptType())
                    .mintPolicyID(d.getMintPolicyID())
                    .contractAddress(d.getContractAddress())
                    .transactionsCount(d.getTransactionsCount())
                    .version(d.getVersion())
                    .updateTime(d.getUpdateTime())
                    .releaseKey(d.getReleaseKey())
                    .name(d.getName())
                    .scriptInvocationsCount(d.getScriptInvocationsCount())
                    .hash(d.getHash())
                    .dappId(d.getDappId())
                    .trxCount(d.getScriptInvocationsCount())
                    .build();
                })
                .toList();

        return DappScriptsResponse.builder()
                .release(b.build())
                .scripts(releaseItems)
                .build();
    }

}
