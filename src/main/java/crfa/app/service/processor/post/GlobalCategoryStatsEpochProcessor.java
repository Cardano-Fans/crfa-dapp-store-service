package crfa.app.service.processor.post;

import crfa.app.domain.*;
import crfa.app.repository.GlobalCategoryStatsEpochRepository;
import crfa.app.repository.epoch.DappsEpochRepository;
import crfa.app.repository.total.DappsRepository;
import crfa.app.service.ScrollsOnChainDataService;
import crfa.app.service.processor.FeedPostProcessor;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import lombok.extern.slf4j.Slf4j;
import lombok.val;

import java.util.Date;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import static crfa.app.domain.InjestionMode.WITHOUT_EPOCHS_ONLY_AGGREGATES;

@Singleton
@Slf4j
public class GlobalCategoryStatsEpochProcessor implements FeedPostProcessor {

    @Inject
    private GlobalCategoryStatsEpochRepository globalCategoryStatsEpochRepository;

    @Inject
    private DappsEpochRepository dappsEpochRepository;

    @Inject
    private DappsRepository dappsRepository;

    @Inject
    private ScrollsOnChainDataService scrollsOnChainDataService;

    public void process(DappFeed dappFeed, InjestionMode injestionMode) {
        if (injestionMode == WITHOUT_EPOCHS_ONLY_AGGREGATES) {
            return;
        }

        val currentEpochNo = scrollsOnChainDataService.currentEpoch().orElseThrow();

        for (val cat: dappsRepository.allCategories()) {
            val epochs = Eras.epochsBetween(SnapshotType.ALL.startEpoch(currentEpochNo), currentEpochNo);

            val injestCurrentEpochOnly = injestionMode == InjestionMode.CURRENT_EPOCH_AND_AGGREGATES;

            if (injestCurrentEpochOnly) {
                val dapps = dappsEpochRepository.list(currentEpochNo);

                val dappPerCat = dapps.stream()
                        .filter(dApp -> dApp.getCategory().equalsIgnoreCase(cat))
                        .collect(Collectors.toSet());

                globalCategoryStatsEpochRepository.upsert(createStats(dappFeed, dappPerCat, currentEpochNo, cat));
            } else {
                for (val epochNo : epochs) {
                    val dapps = dappsEpochRepository.list(epochNo);

                    val dappPerCat = dapps.stream()
                            .filter(dApp -> dApp.getCategory().equalsIgnoreCase(cat))
                            .collect(Collectors.toSet());

                    globalCategoryStatsEpochRepository.upsert(createStats(dappFeed, dappPerCat, epochNo, cat));
                }
            }
        }
    }

    private static GlobalCategoryEpochStats createStats(DappFeed dappFeed,
                                                        Set<DAppEpoch> dappPerCat,
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
        b.spendUniqueAccounts(uniqueAccounts(dappFeed, cat, epochNo));
        b.dapps(dappPerCat.size());

        return b.build();
    }

//    private static int uniqueAccounts(DappFeed dappFeed, String category, int epochNo) {
//        val spendUniqueAccounts = new HashSet<String>();
//
//        for (val dsr : dappFeed.getDappSearchResult()) {
//            if (!dsr.getCategory().equalsIgnoreCase(category)) {
//                continue;
//            }
//
//            for (val r : dsr.getReleases()) {
//                for (val s : r.getScripts()) {
//                    if (s.getPurpose() == SPEND) {
//                        spendUniqueAccounts.addAll(loadSpendUniqueAccounts(dappFeed, s.getUnifiedHash(), epochNo));
//                    }
//                }
//            }
//        }
//
//        return spendUniqueAccounts.size();
//    }

}
