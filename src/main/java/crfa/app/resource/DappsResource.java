package crfa.app.resource;

import crfa.app.domain.SortBy;
import crfa.app.domain.SortOrder;
import crfa.app.repository.total.DappsRepository;
import crfa.app.resource.model.DappResult;
import crfa.app.resource.model.EpochLevelDataResult;
import crfa.app.resource.model.EpochLevelResult;
import crfa.app.resource.model.EpochLevelStatsResult;
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

import static crfa.app.domain.SortBy.SCRIPTS_INVOKED;
import static crfa.app.domain.SortOrder.DESC;

@Controller("/dapps")
@Slf4j
public class DappsResource {

    @Inject
    private DappsRepository dappsRepository;

    @Inject
    private DappService dappService;

    @Get(uri = "/find-dapp/{id}", produces = "application/json")
    @Blocking
    public Optional<DappResult> findDappById(String id) {

        return dappsRepository.findById(id)
                .map(dapp -> {
                    val transactions = dapp.getTransactionsCount();
                    val balance = dapp.getBalance();
                    val volume = dapp.getSpendVolume();
                    val fees = dapp.getSpendTrxFees();
                    val uniqueAccounts = dapp.getSpendUniqueAccounts();

                    val dAppType = dapp.getDAppType();

                    val epochLevelDataResult = dappService.getAllEpochLevelData(dapp)
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

                    return DappResult.builder()
                            .category(dapp.getCategory())
                            .subCategory(dapp.getSubCategory())
                            .dAppType(dAppType)
                            .id(dapp.getId())
                            .icon(dapp.getIcon())
                            .link(dapp.getLink())
                            .name(dapp.getName())
                            .twitter(dapp.getTwitter())
                            .scriptsLocked(balance)
                            .volume(volume)
                            .trxFees(fees)
                            .avgTrxFee(dapp.getAvgTrxFee())
                            .avgTrxSize(dapp.getAvgTrxSize())
                            .uniqueAccounts(uniqueAccounts)
                            .lastVersionContractsOpenSourcedLink(dapp.getLastVersionOpenSourceLink())
                            .lastVersionContractsAuditedLink(dapp.getLastVersionAuditLink())
                            .trxCount(transactions)
                            .updateTime(dapp.getUpdateTime())
                            .epochLevelData(epochLevelDataResult)
                            .build();
                });
    }

    @Get(uri = "/list-dapps", produces = "application/json")
    @Blocking
    public List<DappResult> listDapps(@QueryValue Optional<SortBy> sortBy,
                                      @QueryValue Optional<SortOrder> sortOrder) {
         return dappsRepository.listDapps(sortBy.orElse(SCRIPTS_INVOKED), sortOrder.orElse(DESC))
                .stream()
                 .map(dapp -> {
                    val transactions = dapp.getTransactionsCount();
                    val balance = dapp.getBalance();
                    val volume = dapp.getSpendVolume();
                    val fees = dapp.getSpendTrxFees();
                    val uniqueAccounts = dapp.getSpendUniqueAccounts();

                    val epochLevelDataResult = dappService.getAllEpochLevelData(dapp)
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
                                    .build());

                    return DappResult.builder()
                            .id(dapp.getId())
                            .category(dapp.getCategory())
                            .subCategory(dapp.getSubCategory())
                            .dAppType(dapp.getDAppType())
                            .icon(dapp.getIcon())
                            .link(dapp.getLink())
                            .name(dapp.getName())
                            .twitter(dapp.getTwitter())
                            .scriptsLocked(balance)
                            .volume(volume)
                            .trxFees(fees)
                            .avgTrxSize(dapp.getAvgTrxSize())
                            .avgTrxFee(dapp.getAvgTrxFee())
                            .uniqueAccounts(uniqueAccounts)
                            .lastVersionContractsOpenSourcedLink(dapp.getLastVersionOpenSourceLink())
                            .lastVersionContractsAuditedLink(dapp.getLastVersionAuditLink())
                            .trxCount(transactions)
                            .updateTime(dapp.getUpdateTime())
                            .epochLevelData(epochLevelDataResult)
                            .build();
                }).toList();
    }

}
