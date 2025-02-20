package crfa.app.resource;

import crfa.app.domain.SortBy;
import crfa.app.domain.SortOrder;
import crfa.app.repository.total.DappReleaseRepository;
import crfa.app.resource.model.DappReleaseResult;
import crfa.app.resource.model.EpochLevelDataResult;
import crfa.app.resource.model.EpochLevelResult;
import crfa.app.resource.model.EpochLevelStatsResult;
import crfa.app.service.DappReleaseCacheHelper;
import crfa.app.service.DappService;
import io.micronaut.core.annotation.Blocking;
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

@Controller("/dapps")
@Slf4j
public class DappsReleasesResource {

    @Inject
    private DappReleaseRepository dappReleaseRepository;

    @Inject
    private DappService dappService;

    @Inject
    private DappReleaseCacheHelper dappReleaseCacheHelper;

    @Deprecated
    @Blocking
    @Get(uri = "/list-releases", produces = "application/json")
    public List<DappReleaseResult> listDappReleases(@QueryValue Optional<SortBy> sortBy,
                                                    @QueryValue Optional<SortOrder> sortOrder) {
        val releaseVersionsCache = dappReleaseCacheHelper.buildMaxReleaseVersionCache();

        return dappReleaseRepository.listDappReleases(sortBy.orElse(SCRIPTS_INVOKED), sortOrder.orElse(SortOrder.DESC))
                .stream()
                .map(dAppRelease -> {
                    val maxReleaseVersion = releaseVersionsCache.getIfPresent(dAppRelease.getDappId());

                    val isLastVersion = dAppRelease.isLatestVersion(maxReleaseVersion);

                    val dappType = dAppRelease.getDAppType();

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
                                    //.epochData(e.getEpochData())
                                    .build());

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
                            .fees(dAppRelease.getSpendTrxFees())
                            .avgTrxFee(dAppRelease.getAvgTrxFee())
                            .avgTrxSize(dAppRelease.getAvgTrxSize())
                            .scriptsLocked(dAppRelease.getBalance())
                            .transactionsCount(dAppRelease.getTransactionsCount())
                            .trxCount(dAppRelease.getTransactionsCount())
                            .latestVersion(isLastVersion)
                            .uniqueAccounts(dAppRelease.getSpendUniqueAccounts())
                            .volume(dAppRelease.getSpendVolume())
                            .contractOpenSourcedLink(dAppRelease.getContractLink())
                            .contractsAuditedLink(dAppRelease.getAuditLink())
                            .epochLevelData(epochLevelDataResult)
                            .build();
                }).toList();
    }

    @Get(uri = "/find-release/{id}", produces = "application/json")
    @Blocking
    public List<DappReleaseResult> findDappRelease(String id) {
        return listDappReleases(Optional.of(RELEASE_NUMBER), Optional.of(ASC))
                .stream()
                .filter(dappReleaseResult -> dappReleaseResult.getId().equalsIgnoreCase(id))
                .toList();
    }

}
