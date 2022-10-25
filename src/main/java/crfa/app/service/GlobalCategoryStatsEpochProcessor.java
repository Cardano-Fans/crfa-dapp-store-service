package crfa.app.service;

import crfa.app.domain.*;
import crfa.app.repository.GlobalCategoryStatsEpochRepository;
import crfa.app.repository.epoch.DappsEpochRepository;
import crfa.app.repository.total.DappsRepository;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import lombok.extern.slf4j.Slf4j;
import lombok.val;

import java.util.Date;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import static crfa.app.domain.EraName.ALONZO;

@Singleton
@Slf4j
public class GlobalCategoryStatsEpochProcessor {

    @Inject
    private GlobalCategoryStatsEpochRepository globalStatsRepository;

    @Inject
    private DappsEpochRepository dappsEpochRepository;

    @Inject
    private DappsRepository dappsRepository;

    @Inject
    private DappService dappService;

    public void process(DappFeed dappFeed, InjestionMode injestionMode, FeedProcessingContext context) {
        val currentEpochNo = dappService.currentEpoch();

        for (val cat: dappsRepository.allCategories()) {
            val epochs = Eras.epochsBetween(ALONZO, currentEpochNo);

            val injestCurrentEpochOnly = injestionMode == InjestionMode.CURRENT_EPOCH_AND_AGGREGATES;

            if (injestCurrentEpochOnly) {
                val dapps = dappsEpochRepository.list(currentEpochNo);

                val dappPerCat = dapps.stream()
                        .filter(dApp -> dApp.getCategory().equalsIgnoreCase(cat))
                        .collect(Collectors.toSet());

                globalStatsRepository.upsert(createStats(dappPerCat, currentEpochNo, cat));
            } else {
                for (val epochNo : epochs) {
                    val dapps = dappsEpochRepository.list(epochNo);

                    val dappPerCat = dapps.stream()
                            .filter(dApp -> dApp.getCategory().equalsIgnoreCase(cat))
                            .collect(Collectors.toSet());

                    globalStatsRepository.upsert(createStats(dappPerCat, epochNo, cat));
                }
            }
        }
    }

    private static GlobalCategoryEpochStats createStats(Set<DAppEpoch> dappPerCat,
                                                        int epochNo,
                                                        String cat) {
        val b = GlobalCategoryEpochStats.builder();

        val spendTransactions = dappPerCat.stream().filter(Objects::nonNull).mapToLong(DAppEpoch::getSpendTransactions).sum();
        val mintTransactions = dappPerCat.stream().filter(Objects::nonNull).mapToLong(DAppEpoch::getMintTransactions).sum();

        b.id(String.format("%s.%d", cat, epochNo));
        b.categoryType(cat);
        b.epochNo(epochNo);
        b.updateTime(new Date());
        b.inflowsOutflows(dappPerCat.stream().filter(Objects::nonNull).mapToLong(DAppEpoch::getInflowsOutflows).sum());
        b.spendTransactions(spendTransactions);
        b.mintTransactions(mintTransactions);
        b.spendTrxFees(dappPerCat.stream().filter(Objects::nonNull).mapToLong(DAppEpoch::getSpendTrxFees).sum());
        b.spendTrxSizes(dappPerCat.stream().filter(Objects::nonNull).mapToLong(DAppEpoch::getSpendTrxSizes).sum());
        b.spendVolume(dappPerCat.stream().filter(Objects::nonNull).mapToLong(DAppEpoch::getSpendVolume).sum());
        b.transactions(mintTransactions + spendTransactions);
        b.dapps(dappPerCat.size());

        // TODO unique accounts

        return b.build();
    }

}
