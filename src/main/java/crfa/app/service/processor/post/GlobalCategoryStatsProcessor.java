package crfa.app.service.processor.post;

import crfa.app.domain.DApp;
import crfa.app.domain.DappFeed;
import crfa.app.domain.GlobalCategoryStats;
import crfa.app.domain.InjestionMode;
import crfa.app.repository.GlobalCategoryStatsRepository;
import crfa.app.repository.total.DappsRepository;
import crfa.app.service.processor.FeedPostProcessor;
import crfa.app.service.processor.total.ProcessorHelper;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import lombok.extern.slf4j.Slf4j;
import lombok.val;

import java.util.Date;
import java.util.HashSet;
import java.util.Objects;
import java.util.stream.Collectors;

import static crfa.app.domain.Purpose.SPEND;

@Singleton
@Slf4j
public class GlobalCategoryStatsProcessor implements FeedPostProcessor {

    @Inject
    private GlobalCategoryStatsRepository globalCategoryStatsRepository;

    @Inject
    private DappsRepository dappsRepository;

    public void process(DappFeed dappFeed, InjestionMode injestionMode) {
        val dapps = dappsRepository.listDapps();

        for (val category: dappsRepository.allCategories()) {
            val b = GlobalCategoryStats.builder();

            val dappPerCat = dapps.stream()
                    .filter(dApp -> dApp.getCategory().equalsIgnoreCase(category))
                    .collect(Collectors.toSet());

            val spendTransactions = dappPerCat.stream().filter(Objects::nonNull).mapToLong(DApp::getSpendTransactions).sum();
            val mintTransactions = dappPerCat.stream().filter(Objects::nonNull).mapToLong(DApp::getMintTransactions).sum();

            b.categoryType(category);
            b.updateTime(new Date());
            b.balance(dappPerCat.stream().filter(Objects::nonNull).mapToLong(DApp::getBalance).sum());
            b.spendTransactions(spendTransactions);
            b.mintTransactions(mintTransactions);
            b.spendTrxFees(dappPerCat.stream().filter(Objects::nonNull).mapToLong(DApp::getSpendTrxFees).sum());
            b.spendTrxSizes(dappPerCat.stream().filter(Objects::nonNull).mapToLong(DApp::getSpendTrxSizes).sum());
            b.spendVolume(dappPerCat.stream().filter(Objects::nonNull).mapToLong(DApp::getSpendVolume).sum());
            b.transactions(mintTransactions + spendTransactions);
            b.spendUniqueAccounts(uniqueAccounts(category, dappFeed));

            b.dapps(dappPerCat.size());

            globalCategoryStatsRepository.upsert(b.build());
        }

    }

    private int uniqueAccounts(String category, DappFeed dappFeed) {
        val spendUniqueAccounts = new HashSet<String>();

        for (val dsr : dappFeed.getDappSearchResult()) {
            if (!dsr.getCategory().equalsIgnoreCase(category)) {
                continue;
            }
            for (val r : dsr.getReleases()) {
                for (val s : r.getScripts()) {
                    if (s.getPurpose() == SPEND) {
                        spendUniqueAccounts.addAll(ProcessorHelper.loadSpendUniqueAccounts(dappFeed, s.getUnifiedHash()));
                    }
                }
            }
        }

        return spendUniqueAccounts.size();
    }

}
