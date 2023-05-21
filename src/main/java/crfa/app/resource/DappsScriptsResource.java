package crfa.app.resource;

import crfa.app.domain.DAppRelease;
import crfa.app.domain.DappScriptItem;
import crfa.app.domain.SortBy;
import crfa.app.domain.SortOrder;
import crfa.app.repository.total.DappReleaseRepository;
import crfa.app.repository.total.DappScriptsRepository;
import crfa.app.resource.model.*;
import crfa.app.service.DappReleaseCacheHelper;
import crfa.app.service.DappService;
import crfa.app.utils.Json;
import io.micronaut.core.annotation.Blocking;
import io.micronaut.http.annotation.Controller;
import io.micronaut.http.annotation.Get;
import io.micronaut.http.annotation.PathVariable;
import io.micronaut.http.annotation.QueryValue;
import jakarta.inject.Inject;
import lombok.extern.slf4j.Slf4j;
import lombok.val;

import java.util.List;
import java.util.Optional;

@Controller("/dapps")
@Slf4j
public class DappsScriptsResource {

    @Inject
    private DappReleaseCacheHelper dappReleaseCacheHelper;

    @Inject
    private DappReleaseRepository dappReleaseRepository;

    @Inject
    private DappScriptsRepository dappScriptsRepository;

    @Inject
    private DappService dappService;

    @Inject
    private Json json;

    @Get(uri = "/by-release-key/{releaseKey}", produces = "application/json")
    @Blocking
    public Optional<DappScriptsResult> listScriptsResponse(@PathVariable String releaseKey,
                                                           @QueryValue Optional<SortBy> sortBy,
                                                           @QueryValue Optional<SortOrder> sortOrder) {
        val dappScriptItems = dappScriptsRepository.listDappScriptItems(releaseKey, sortBy.orElse(SortBy.SCRIPTS_INVOKED), sortOrder.orElse(SortOrder.DESC));

        return dappReleaseRepository.findById(releaseKey)
                .map(dAppRelease -> {
                    return DappScriptsResult.builder()
                            .release(getDappReleaseResult(dAppRelease))
                            .scripts(getdAppScriptItemResults(dappScriptItems))
                            .build();
                });
    }

    private DappReleaseResult getDappReleaseResult(DAppRelease dAppRelease) {
        val releaseVersionsCache = dappReleaseCacheHelper.buildMaxReleaseVersionCache();

        val scriptInvocationsCount = dAppRelease.getSpendTransactions();
        val uniqueAccounts = dAppRelease.getSpendUniqueAccounts();

        val maxReleaseVersion = releaseVersionsCache.getIfPresent(dAppRelease.getDappId());

        val isLastVersion = dAppRelease.isLatestVersion(maxReleaseVersion);

        val epochLevelDataResult = dappService.getAllEpochLevelData(dAppRelease)
                .map(e -> EpochLevelDataResult.builder()
                        .lastMonthDeltaWithOnlyClosedEpochs(e.getLastMonthDeltaWithOnlyClosedEpochs().map(d -> EpochLevelResult.builder()
                                .from(d.getFrom().getStartEpoch())
                                .to(d.getTo().getEndEpoch())
                                .snapshot(EpochLevelStatsResult.create(d.getSnapshot()))
                                .activityDiffPerc(d.activityDiffPerc())
                                .build()))
                        .lastQuarterDeltaWithOnlyClosedEpochs(e.getLastQuarterDeltaWithOnlyClosedEpochs().map(d -> {
                            return EpochLevelResult.builder()
                                    .from(d.getFrom().getStartEpoch())
                                    .to(d.getTo().getEndEpoch())
                                    .snapshot(EpochLevelStatsResult.create(d.getSnapshot()))
                                    .activityDiffPerc(d.activityDiffPerc())
                                    .build();
                        }))
                        .lastEpochDeltaWithOnlyClosedEpochs(e.getLastEpochDeltaWithOnlyClosedEpochs().map(d -> {
                            return EpochLevelResult.builder()
                                    .from(d.getFrom().getStartEpoch())
                                    .to(d.getTo().getEndEpoch())
                                    .snapshot(EpochLevelStatsResult.create(d.getSnapshot()))
                                    .activityDiffPerc(d.activityDiffPerc())
                                    .build();
                        }))
                        .epochData(e.getEpochData())
                        .build());

        return DappReleaseResult.builder()
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
                .transactionsCount(scriptInvocationsCount)
                .scriptsLocked(dAppRelease.getBalance())
                .trxCount(scriptInvocationsCount)
                .volume(dAppRelease.getSpendVolume())
                .fees(dAppRelease.getSpendTrxFees() + 0)
                .avgTrxFee(dAppRelease.getAvgTrxFee())
                .avgTrxSize(dAppRelease.getAvgTrxSize())
                .uniqueAccounts(uniqueAccounts)
                .latestVersion(isLastVersion)
                .contractOpenSourcedLink(dAppRelease.getContractLink())
                .contractsAuditedLink(dAppRelease.getAuditLink())
                .epochLevelData(epochLevelDataResult)
                .build();
    }

    private List<DAppScriptItemResult> getdAppScriptItemResults(List<DappScriptItem> dappScriptItems) {
        return dappScriptItems
                .stream()
                .map(dappScriptItem -> {
                    val epochLevelDataResult = dappService.getAllEpochLevelData(dappScriptItem)
                            .map(e -> EpochLevelDataResult.builder()
                                    .lastMonthDeltaWithOnlyClosedEpochs(e.getLastMonthDeltaWithOnlyClosedEpochs().map(d -> EpochLevelResult.builder()
                                            .from(d.getFrom().getStartEpoch())
                                            .to(d.getTo().getEndEpoch())
                                            .snapshot(EpochLevelStatsResult.create(d.getSnapshot()))
                                            .activityDiffPerc(d.activityDiffPerc())
                                            .build()))
                                    .lastQuarterDeltaWithOnlyClosedEpochs(e.getLastQuarterDeltaWithOnlyClosedEpochs().map(d -> {
                                        return EpochLevelResult.builder()
                                                .from(d.getFrom().getStartEpoch())
                                                .to(d.getTo().getEndEpoch())
                                                .snapshot(EpochLevelStatsResult.create(d.getSnapshot()))
                                                .activityDiffPerc(d.activityDiffPerc())
                                                .build();
                                    }))
                                    .lastEpochDeltaWithOnlyClosedEpochs(e.getLastEpochDeltaWithOnlyClosedEpochs().map(d -> {
                                        return EpochLevelResult.builder()
                                                .from(d.getFrom().getStartEpoch())
                                                .to(d.getTo().getEndEpoch())
                                                .snapshot(EpochLevelStatsResult.create(d.getSnapshot()))
                                                .activityDiffPerc(d.activityDiffPerc())
                                                .build();
                                    }))
                                    //.epochData(e.getEpochData())
                                    .build());

                    return DAppScriptItemResult.builder()
                            .dappId(dappScriptItem.getDappId())
                            .hash(dappScriptItem.getHash())
                            .scriptsLocked(dappScriptItem.getBalance())
                            .scriptType(dappScriptItem.getScriptType())
                            .mintPolicyID(dappScriptItem.getMintPolicyID())
                            .version(dappScriptItem.getVersion())
                            .updateTime(dappScriptItem.getUpdateTime())
                            .releaseKey(dappScriptItem.getReleaseKey())
                            .name(dappScriptItem.getName())
                            .hash(dappScriptItem.getHash())
                            .dappId(dappScriptItem.getDappId())
                            .trxCount(dappScriptItem.getTransactions())
                            .poolData(json.read(dappScriptItem.getPool()))
                            .uniqueAccounts(dappScriptItem.getUniqueAccounts())
                            .volume(dappScriptItem.getVolume())
                            .fees(dappScriptItem.getTrxFees())
                            .avgTrxFee(dappScriptItem.getAvgTrxFee())
                            .avgTrxSize(dappScriptItem.getAvgTrxSize())
                            .plutusVersion(dappScriptItem.getPlutusVersion())
                            .epochLevelData(epochLevelDataResult)
                            .build();
                })
                .toList();
    }

}
