package crfa.app.resource;

import crfa.app.domain.DappAggrType;
import crfa.app.domain.EpochKey;
import crfa.app.domain.SortBy;
import crfa.app.domain.SortOrder;
import crfa.app.repository.epoch.DappReleaseEpochRepository;
import crfa.app.repository.epoch.DappScriptsEpochRepository;
import crfa.app.repository.total.DappReleaseRepository;
import crfa.app.repository.total.DappScriptsRepository;
import crfa.app.repository.total.DappsRepository;
import crfa.app.resource.model.*;
import crfa.app.service.DappService;
import io.micronaut.http.annotation.Controller;
import io.micronaut.http.annotation.Get;
import io.micronaut.http.annotation.PathVariable;
import io.micronaut.http.annotation.QueryValue;
import jakarta.inject.Inject;
import lombok.extern.slf4j.Slf4j;
import lombok.val;

import java.util.*;
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
    private DappReleaseEpochRepository dappReleaseEpochRepository;

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
                    val maxReleaseVersion = releaseVersionsCache.getIfPresent(dAppRelease.getDappId());

                    val isLastVersion = dAppRelease.isLatestVersion(maxReleaseVersion);

                    return  DappReleaseResult.builder()
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
                            .scriptsLocked(dAppRelease.getScriptsLocked())
                            .transactionsCount(dAppRelease.getTransactionsCount())
                            .trxCount(dAppRelease.getScriptInvocationsCount())
                            .latestVersion(isLastVersion)
                            .uniqueAccounts(dAppRelease.getUniqueAccounts())
                            .volume(dAppRelease.getVolume())
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

    @Get(uri = "/find-dapp/{id}", produces = "application/json")
    public Optional<DappResult> findDappById(String id, @QueryValue Optional<DappAggrType> dappAggrType) {
        val dappAggrTypeWithFallback = dappAggrType.orElse(DappAggrType.def());

        return dappsRepository.findById(id)
                .map(dapp -> {
                    val scriptInvocationsCount = dappAggrTypeWithFallback == LAST ? dapp.getLastVersionScriptInvocationsCount() : dapp.getScriptInvocationsCount();
                    val scriptsLocked = dappAggrTypeWithFallback == LAST ? dapp.getLastVersionScriptsLocked() : dapp.getScriptsLocked();
                    val volume = dappAggrTypeWithFallback == LAST ? dapp.getLastVersionVolume() : dapp.getVolume();
                    val uniqueAccounts = dappAggrTypeWithFallback == LAST ? dapp.getLastVersionUniqueAccounts() : dapp.getUniqueAccounts();

                    return DappResult.builder()
                            .category(dapp.getCategory())
                            .subCategory(dapp.getSubCategory())
                            .dAppType(dapp.getDAppType())
                            .id(dapp.getId())
                            .icon(dapp.getIcon())
                            .link(dapp.getLink())
                            .name(dapp.getName())
                            .twitter(dapp.getTwitter())
                            .scriptsLocked(scriptsLocked)
                            .volume(volume)
                            .uniqueAccounts(uniqueAccounts)
                            .lastVersionContractsOpenSourcedLink(dapp.getLastVersionOpenSourceLink())
                            .lastVersionContractsAuditedLink(dapp.getLastVersionAuditLink())
                            .trxCount(scriptInvocationsCount)
                            .updateTime(dapp.getUpdateTime())
                            .build();
                });
    }

                                      @Get(uri = "/list-dapps", produces = "application/json")
    public List<DappResult> listDapps(@QueryValue Optional<SortBy> sortBy,
                                      @QueryValue Optional<SortOrder> sortOrder,
                                      @QueryValue Optional<DappAggrType> dappAggrType) throws InvalidParameterException {
        val dappAggrTypeWithFallback = dappAggrType.orElse(DappAggrType.def());

        return dappsRepository.listDapps(sortBy, sortOrder, dappAggrTypeWithFallback)
                .stream().map(dapp -> {
                    val scriptInvocationsCount = dappAggrTypeWithFallback == LAST ? dapp.getLastVersionScriptInvocationsCount() : dapp.getScriptInvocationsCount();
                    val scriptsLocked = dappAggrTypeWithFallback == LAST ? dapp.getLastVersionScriptsLocked() : dapp.getScriptsLocked();
                    val volume = dappAggrTypeWithFallback == LAST ? dapp.getLastVersionVolume() : dapp.getVolume();
                    val uniqueAccounts = dappAggrTypeWithFallback == LAST ? dapp.getLastVersionUniqueAccounts() : dapp.getUniqueAccounts();

                    return DappResult.builder()
                            .category(dapp.getCategory())
                            .subCategory(dapp.getSubCategory())
                            .dAppType(dapp.getDAppType())
                            .id(dapp.getId())
                            .icon(dapp.getIcon())
                            .link(dapp.getLink())
                            .name(dapp.getName())
                            .twitter(dapp.getTwitter())
                            .scriptsLocked(scriptsLocked)
                            .volume(volume)
                            .uniqueAccounts(uniqueAccounts)
                            .lastVersionContractsOpenSourcedLink(dapp.getLastVersionOpenSourceLink())
                            .lastVersionContractsAuditedLink(dapp.getLastVersionAuditLink())
                            .trxCount(scriptInvocationsCount)
                            .updateTime(dapp.getUpdateTime())
                            .build();
                }).toList();
    }

    @Get(uri = "/by-release-key/{releaseKey}", produces = "application/json")
    public Optional<DappScriptsResponse> listScriptsResponse(@PathVariable String releaseKey,
                                                   @QueryValue Optional<SortBy> sortBy,
                                                   @QueryValue Optional<SortOrder> sortOrder) throws InvalidParameterException {
        val dappScriptItems = dappScriptsRepository.listDappScriptItems(releaseKey, sortBy, sortOrder);

        return dappReleaseRepository.findById(releaseKey)
                .map(dAppRelease -> {
                    val releaseVersionsCache = dappService.buildMaxReleaseVersionCache();

                    val scriptInvocationsCount = dAppRelease.getScriptInvocationsCount();
                    val uniqueAccounts = dAppRelease.getUniqueAccounts();
                    val transactionsCount = dAppRelease.getTransactionsCount();

                    val maxReleaseVersion = releaseVersionsCache.getIfPresent(dAppRelease.getDappId());

                    val isLastVersion = dAppRelease.isLatestVersion(maxReleaseVersion);

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
                            .epochData(gatherEpochLevelDataForDappRelease(releaseKey))
                            .build();

                    val epochLevelData = gatherEpochLevelDataForScriptItems(releaseKey);

                    val dAppScriptItemResults = dappScriptItems
                            .stream()
                            .map(dappScriptItem -> {
                                val contractAddress = dappScriptItem.getContractAddress();
                                val scriptEpochLevelData = epochLevelData.entrySet()
                                        .stream()
                                        .filter(entry -> contractAddress != null && contractAddress.equals(entry.getKey().getValue()))
                                        .map(entry -> new AbstractMap.SimpleEntry<>(entry.getKey().getEpochNo(), entry.getValue()))
                                        .collect(Collectors.toMap(
                                                Map.Entry::getKey,
                                                Map.Entry::getValue
                                        ));

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
                                        .epochData(scriptEpochLevelData)
                                        .build();
                            })
                            .toList();

                    return DappScriptsResponse.builder()
                            .release(dappReleaseResult)
                            .scripts(dAppScriptItemResults)
                            .build();
                });
    }
    private Map<EpochKey<String>, EpochLevelStats> gatherEpochLevelDataForScriptItems(@PathVariable String releaseKey) {
        val epochLevelStats = new HashMap<EpochKey<String>, EpochLevelStats>();

        try {
            val epochItems = dappScriptsEpochRepository.listDappScriptItems(releaseKey, Optional.empty(), Optional.empty());
            epochItems.forEach(dappScriptItemEpoch -> {
                val epochNo = dappScriptItemEpoch.getEpochNo();

                epochLevelStats.put(new EpochKey<>(epochNo, dappScriptItemEpoch.getContractAddress()), EpochLevelStats.builder()
                        .volume(dappScriptItemEpoch.getVolume())
                        .inflowsOutflows(dappScriptItemEpoch.getInflowsOutflows())
                        .uniqueAccounts(dappScriptItemEpoch.getUniqueAccounts())
                        .trxCount(dappScriptItemEpoch.getScriptInvocationsCount())
                        .build()
                );
            });

            return epochLevelStats;
        } catch (InvalidParameterException e) {
            throw new RuntimeException(e);
        }
    }

    private Map<Integer, EpochLevelStats> gatherEpochLevelDataForDappRelease(@PathVariable String releaseKey) {
        val epochLevelStats = new HashMap<Integer, EpochLevelStats>();

        val epochItems = dappReleaseEpochRepository.findByReleaseKey(releaseKey);

        epochItems.forEach(dappScriptItemEpoch -> {
            val epochNo = dappScriptItemEpoch.getEpochNo();

            epochLevelStats.put(epochNo, EpochLevelStats.builder()
                            .volume(dappScriptItemEpoch.getVolume())
                            .inflowsOutflows(dappScriptItemEpoch.getInflowsOutflows())
                            .uniqueAccounts(dappScriptItemEpoch.getUniqueAccounts())
                            .trxCount(dappScriptItemEpoch.getScriptInvocationsCount())
                            .build()
            );
        });

        return epochLevelStats;
    }

}
