package crfa.app.resource;

import crfa.app.domain.SortBy;
import crfa.app.domain.SortOrder;
import crfa.app.repository.epoch.DappReleaseEpochRepository;
import crfa.app.repository.epoch.DappScriptsEpochRepository;
import crfa.app.repository.total.DappReleaseRepository;
import crfa.app.repository.total.DappScriptsRepository;
import crfa.app.resource.model.DAppScriptItemResult;
import crfa.app.resource.model.DappReleaseResult;
import crfa.app.resource.model.DappScriptsResponse;
import crfa.app.service.DappService;
import io.micronaut.http.annotation.Controller;
import io.micronaut.http.annotation.Get;
import io.micronaut.http.annotation.PathVariable;
import io.micronaut.http.annotation.QueryValue;
import jakarta.inject.Inject;
import lombok.extern.slf4j.Slf4j;
import lombok.val;

import java.util.Optional;

@Controller("/dapps")
@Slf4j
public class DappsScriptsResource {

    @Inject
    private DappReleaseRepository dappReleaseRepository;

    @Inject
    private DappReleaseEpochRepository dappReleaseEpochRepository;

    @Inject
    private DappScriptsRepository dappScriptsRepository;

    @Inject
    private DappScriptsEpochRepository dappScriptsEpochRepository;

    @Inject
    private DappService dappService;


    @Get(uri = "/by-release-key/{releaseKey}", produces = "application/json")
    public Optional<DappScriptsResponse> listScriptsResponse(@PathVariable String releaseKey,
                                                   @QueryValue Optional<SortBy> sortBy,
                                                   @QueryValue Optional<SortOrder> sortOrder) throws InvalidParameterException {
        val dappScriptItems = dappScriptsRepository.listDappScriptItems(releaseKey, sortBy, sortOrder);

        val epochLevelData = dappService.gatherEpochLevelData(dappScriptsEpochRepository.listDappScriptItems(releaseKey));

        return dappReleaseRepository.findById(releaseKey)
                .map(dAppRelease -> {
                    val releaseVersionsCache = dappService.buildMaxReleaseVersionCache();

                    val scriptInvocationsCount = dAppRelease.getScriptInvocationsCount();
                    val uniqueAccounts = dAppRelease.getUniqueAccounts();
                    val transactionsCount = dAppRelease.getTransactionsCount();

                    val maxReleaseVersion = releaseVersionsCache.getIfPresent(dAppRelease.getDappId());

                    val isLastVersion = dAppRelease.isLatestVersion(maxReleaseVersion);

                    val releaseLevelEpochData = dappService.gatherEpochLevelData(dappReleaseEpochRepository.findByReleaseKey(releaseKey));

                    val dappReleaseResult = DappReleaseResult.builder()
                            .category(dAppRelease.getCategory())
                            .subCategory(dAppRelease.getSubCategory())
                            .dAppType(dAppRelease.getDAppType())
                            .fullName(dAppRelease.getFullName())
                            .id(dAppRelease.getDappId())
                            .icon(dAppRelease.getIcon())
                            .key(dAppRelease.getId())
                            .link(dAppRelease.getLink())
                            .releaseName(dAppRelease.getReleaseName())
                            .name(dAppRelease.getName())
                            .twitter(dAppRelease.getTwitter())
                            .updateTime(dAppRelease.getUpdateTime())
                            .releaseNumber(dAppRelease.getReleaseNumber())
                            .transactionsCount(transactionsCount)
                            .scriptsLocked(dAppRelease.getScriptsLocked())
                            .trxCount(scriptInvocationsCount)
                            .volume(dAppRelease.getVolume())
                            .uniqueAccounts(uniqueAccounts)
                            .latestVersion(isLastVersion)
                            .contractOpenSourcedLink(dAppRelease.getContractLink())
                            .contractsAuditedLink(dAppRelease.getAuditLink())
                            .epochData(releaseLevelEpochData)
                            .lastClosedEpochsDelta(dappService.getLastClosedEpochsDelta(releaseLevelEpochData).orElse(null))
                            .build();

                    val dAppScriptItemResults = dappScriptItems
                            .stream()
                            .map(dappScriptItem -> {
                                return DAppScriptItemResult.builder()
                                        .dappId(dappScriptItem.getDappId())
                                        .hash(dappScriptItem.getHash())
                                        .scriptsLocked(dappScriptItem.getScriptsLocked())
                                        .scriptType(dappScriptItem.getScriptType())
                                        .mintPolicyID(dappScriptItem.getMintPolicyID())
                                        .contractAddress(dappScriptItem.getContractAddress())
                                        .version(dappScriptItem.getVersion())
                                        .updateTime(dappScriptItem.getUpdateTime())
                                        .releaseKey(dappScriptItem.getReleaseKey())
                                        .name(dappScriptItem.getName())
                                        .hash(dappScriptItem.getHash())
                                        .dappId(dappScriptItem.getDappId())
                                        .trxCount(dappScriptItem.getScriptInvocationsCount())
                                        .uniqueAccounts(dappScriptItem.getUniqueAccounts())
                                        .volume(dappScriptItem.getVolume())
                                        .epochData(epochLevelData)
                                        .lastClosedEpochsDelta(dappService.getLastClosedEpochsDelta(epochLevelData).orElse(null))
                                        .build();
                            })
                            .toList();

                    return DappScriptsResponse.builder()
                            .release(dappReleaseResult)
                            .scripts(dAppScriptItemResults)
                            .build();
                });
    }

}
