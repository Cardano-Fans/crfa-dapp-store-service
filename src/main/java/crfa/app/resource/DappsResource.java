package crfa.app.resource;

import crfa.app.domain.DappAggrType;
import crfa.app.domain.SortBy;
import crfa.app.domain.SortOrder;
import crfa.app.repository.DappReleaseRepository;
import crfa.app.repository.DappScriptsEpochRepository;
import crfa.app.repository.DappScriptsRepository;
import crfa.app.repository.DappsRepository;
import crfa.app.resource.model.DAppScriptItemResult;
import crfa.app.resource.model.DappReleaseResult;
import crfa.app.resource.model.DappResult;
import crfa.app.resource.model.DappScriptsResponse;
import crfa.app.service.DappService;
import io.micronaut.http.annotation.Controller;
import io.micronaut.http.annotation.Get;
import io.micronaut.http.annotation.PathVariable;
import io.micronaut.http.annotation.QueryValue;
import jakarta.inject.Inject;
import lombok.extern.slf4j.Slf4j;
import lombok.val;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
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
    private DappReleaseRepository dappReleaseRepository;

    @Inject
    private DappScriptsRepository dappScriptsRepository;

    @Inject
    private DappScriptsEpochRepository dappScriptsEpochRepository;

    @Inject
    private DappService dappService;

    @Get(uri = "/list-releases", produces = "application/json")
    public List<DappReleaseResult> listDappReleases(@QueryValue Optional<SortBy> sortBy,
                                                    @QueryValue Optional<SortOrder> sortOrder) throws InvalidParameterException {

        val releaseVersionsCache = dappService.buildMaxReleaseVersionCache();

        return dappReleaseRepository.listDappReleases(sortBy, sortOrder)
                .stream().map(dAppRelease -> {
                    val maxReleaseVersion = releaseVersionsCache.getIfPresent(dAppRelease.getId());

                    val isLastVersion = dAppRelease.isLatestVersion(maxReleaseVersion);

                    return  DappReleaseResult.builder()
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
                            .latestVersion(isLastVersion)
                            .contractOpenSourcedLink(dAppRelease.getContractLink())
                            .contractsAuditedLink(dAppRelease.getAuditLink())
                            .build();
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

        val dappAggrTypeWithFallback = dappAggrType.orElse(DappAggrType.def());

        return dappsRepository.listDapps(sortBy, sortOrder, dappAggrTypeWithFallback)
                .stream().map(dapp -> {
                    //val transactionsCount = dappAggrTypeWithFallback == LAST ? dapp.getLastVersionTransactionsCount() : dapp.getTransactionsCount();
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
                            //.transactionsCount(transactionsCount)
                            .scriptsLocked(scriptsLocked)
                            //.scriptInvocationsCount(scriptInvocationsCount)
                            //.lastVersionContractsOpenSourced(dapp.getLastVersionOpenSourceLink() != null)
                            //.lastVersionContractsAudited(dapp.getLastVersionAuditLink() != null)
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
        val maybeDappRelease = dappReleaseRepository.findByReleaseKey(releaseKey);

        if (maybeDappRelease.isEmpty()) {
            throw new DappReleaseNotFoundException("Dapp release key not found: " + releaseKey);
        }

        val releaseVersionsCache = dappService.buildMaxReleaseVersionCache();

        val dAppRelease = maybeDappRelease.get();

        val scriptInvocationsCount = dAppRelease.getScriptInvocationsCount();
        //val transactionsCount = dAppRelease.getTransactionsCount();

        val maxReleaseVersion = releaseVersionsCache.getIfPresent(dAppRelease.getId());

        val isLastVersion = dAppRelease.isLatestVersion(maxReleaseVersion);

        val dappReleaseResult = DappReleaseResult.builder()
                //.scriptInvocationsCount(scriptInvocationsCount)
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
                //.transactionsCount(transactionsCount)
                .scriptsLocked(dAppRelease.getScriptsLocked())
                .trxCount(scriptInvocationsCount)
                .latestVersion(isLastVersion)
                .contractOpenSourcedLink(dAppRelease.getContractLink())
                .contractsAuditedLink(dAppRelease.getAuditLink())
                .build();

        val epochLevelData = gatherEpochLevelData(releaseKey, sortBy, sortOrder);

        val dAppScriptItemResults = dappScriptsRepository.listDappScriptItems(releaseKey, sortBy, sortOrder)
                .stream()
                .map(item -> DAppScriptItemResult.builder()
                .dappId(item.getDappId())
                .hash(item.getHash())
                .scriptsLocked(item.getScriptsLocked())
                .scriptType(item.getScriptType())
                .mintPolicyID(item.getMintPolicyID())
                .contractAddress(item.getContractAddress())
                //.transactionsCount(item.getTransactionsCount())
                .version(item.getVersion())
                .updateTime(item.getUpdateTime())
                .releaseKey(item.getReleaseKey())
                .name(item.getName())
                //.scriptInvocationsCount(item.getScriptInvocationsCount())
                .hash(item.getHash())
                .dappId(item.getDappId())
                .trxCount(item.getScriptInvocationsCount())
                .epochData(epochLevelData)
                .build())
                .toList();

        return DappScriptsResponse.builder()
                .release(dappReleaseResult)
                .scripts(dAppScriptItemResults)
                .build();
    }

    private Map<Integer, DAppScriptItemResult.EpochStats> gatherEpochLevelData(@PathVariable String releaseKey,
                                                                               @QueryValue Optional<SortBy> sortBy,
                                                                               @QueryValue Optional<SortOrder> sortOrder) throws InvalidParameterException {
        val epochLevelStats = new HashMap<Integer, DAppScriptItemResult.EpochStats>();

        val epochItems = dappScriptsEpochRepository.listDappScriptItems(releaseKey, sortBy, sortOrder);

        epochItems.stream()
                .filter(item ->
                        (item.getScriptInvocationsCount() > 0)
                        ||
                        (item.getVolume() != null && item.getVolume() > 0))
                .forEach(dappScriptItemEpoch -> {
            epochLevelStats.put(dappScriptItemEpoch.getEpochNo(), DAppScriptItemResult.EpochStats.builder()
                            .volume(dappScriptItemEpoch.getVolume())
                            .balance(dappScriptItemEpoch.getBalance())
                            .trxCount(dappScriptItemEpoch.getScriptInvocationsCount())
//                            .scriptInvocationsCount(dappScriptItemEpoch.getScriptInvocationsCount())
//                            .transactionsCount(dappScriptItemEpoch.getTransactionsCount())
                    .build()
            );
        });

        return epochLevelStats;
    }

}
