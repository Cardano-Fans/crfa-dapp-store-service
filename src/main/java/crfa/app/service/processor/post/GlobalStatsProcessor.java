package crfa.app.service.processor.post;

import crfa.app.domain.AdaPricePerDay;
import crfa.app.domain.DappFeed;
import crfa.app.domain.GlobalStats;
import crfa.app.domain.InjestionMode;
import crfa.app.repository.GlobalStatsRepository;
import crfa.app.repository.total.AdaPriceRepository;
import crfa.app.repository.total.DappsRepository;
import crfa.app.service.ScrollsOnChainDataService;
import crfa.app.service.processor.FeedPostProcessor;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import lombok.extern.slf4j.Slf4j;
import lombok.val;

import java.math.BigDecimal;
import java.util.Date;

import static crfa.app.domain.SortBy.SCRIPTS_INVOKED;
import static crfa.app.domain.SortOrder.DESC;

@Singleton
@Slf4j
public class GlobalStatsProcessor implements FeedPostProcessor {

    @Inject
    private GlobalStatsRepository globalStatsRepository;

    @Inject
    private DappsRepository dappsRepository;

    @Inject
    private AdaPriceRepository adaPriceRepository;

    @Inject
    private ScrollsOnChainDataService scrollsOnChainDataService;

    public void process(DappFeed dappFeed, InjestionMode injestionMode) {
        val b = GlobalStats.builder();

        b.id("global");
        b.updateTime(new Date());

        b.adaPriceEUR(priceForCurrency("EUR"));
        b.adaPriceUSD(priceForCurrency("USD"));

        val spendTransactions = dappsRepository.spendTransactions();
        val mintTransactions = dappsRepository.mintTransactions();

        b.balance(dappsRepository.balance());
        b.spendTransactions(spendTransactions);
        b.mintTransactions(mintTransactions);
        b.spendVolume(dappsRepository.spendVolume());
        b.spendTrxFees(dappsRepository.spendTrxFees());
        b.spendTrxSizes(dappsRepository.spendTrxSizes());
        b.transactions(spendTransactions + mintTransactions);

        b.dapps(dapps());

        b.spendUniqueAccounts(scrollsOnChainDataService.getStoreGlobalUniqueAccountsCount());

        globalStatsRepository.upsert(b.build());
    }

    private BigDecimal priceForCurrency(String currency) {
        return adaPriceRepository.getLatestPrice(currency)
                .map(AdaPricePerDay::getPrice)
                .map(BigDecimal::valueOf)
                .orElseThrow();
    }

    private int dapps() {
        return dappsRepository.listDapps(SCRIPTS_INVOKED, DESC).size();
    }

}
