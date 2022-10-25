package crfa.app.service;

import crfa.app.domain.*;
import crfa.app.repository.GlobalCategoryStatsRepository;
import crfa.app.repository.total.DappsRepository;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import lombok.extern.slf4j.Slf4j;
import lombok.val;

import java.util.Date;
import java.util.Objects;
import java.util.stream.Collectors;

@Singleton
@Slf4j
public class GlobalCategoryStatsProcessor {

    @Inject
    private GlobalCategoryStatsRepository globalStatsRepository;

    @Inject
    private DappsRepository dappsRepository;

    public void process(DappFeed dappFeed, InjestionMode injestionMode, FeedProcessingContext context) {
        val dapps = dappsRepository.listDapps();

        for (val cat: dappsRepository.allCategories()) {
            val b = GlobalCategoryStats.builder();

            val dappPerCat = dapps.stream()
                    .filter(dApp -> dApp.getCategory().equalsIgnoreCase(cat))
                    .collect(Collectors.toSet());

            val spendTransactions = dappPerCat.stream().filter(Objects::nonNull).mapToLong(DApp::getSpendTransactions).sum();
            val mintTransactions = dappPerCat.stream().filter(Objects::nonNull).mapToLong(DApp::getMintTransactions).sum();

            b.categoryType(cat);
            b.updateTime(new Date());
            b.balance(dappPerCat.stream().filter(Objects::nonNull).mapToLong(DApp::getBalance).sum());
            b.spendTransactions(spendTransactions);
            b.mintTransactions(mintTransactions);
            b.spendTrxFees(dappPerCat.stream().filter(Objects::nonNull).mapToLong(DApp::getSpendTrxFees).sum());
            b.spendTrxSizes(dappPerCat.stream().filter(Objects::nonNull).mapToLong(DApp::getSpendTrxSizes).sum());
            b.spendVolume(dappPerCat.stream().filter(Objects::nonNull).mapToLong(DApp::getSpendVolume).sum());
            b.transactions(mintTransactions + spendTransactions);

            // TODO unique accounts

            b.dapps(dappPerCat.size());

            globalStatsRepository.upsert(b.build());
        }

    }

}
