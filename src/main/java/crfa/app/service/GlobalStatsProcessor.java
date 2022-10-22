package crfa.app.service;

import crfa.app.domain.*;
import crfa.app.repository.GlobalStatsRepository;
import crfa.app.repository.total.AdaPriceRepository;
import crfa.app.repository.total.DappsRepository;
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
public class GlobalStatsProcessor {

    @Inject
    private GlobalStatsRepository globalStatsRepository;

    @Inject
    private DappsRepository dappsRepository;

    @Inject
    private AdaPriceRepository adaPriceRepository;


    public void process(DappFeed dappFeed, InjestionMode injestionMode, FeedProcessingContext context) {
        val b = GlobalStats.builder();

        b.id("global");
        b.updateTime(new Date());

        b.adaPriceEUR(priceForCurrency("EUR"));
        b.adaPriceUSD(priceForCurrency("USD"));

        b.totalScriptsLocked(dappsRepository.totalScriptsLocked());
        b.totalTrxCount(dappsRepository.totalScriptInvocations());
        b.totalVolume(dappsRepository.volume());
        b.totalFees(dappsRepository.fees());
        b.totalTrxSizes(dappsRepository.totalTrxSizes());

        b.totalDapps(totalDapps());

        b.totalUniqueAccounts(context.getUniqueAccounts().size());

        globalStatsRepository.upsert(b.build());
    }

    private BigDecimal priceForCurrency(String currency) {
        return adaPriceRepository.getLatestPrice(currency)
                .map(AdaPricePerDay::getPrice)
                .map(BigDecimal::valueOf)
                .orElseThrow();
    }

    private int totalDapps() {
        return dappsRepository.listDapps(SCRIPTS_INVOKED, DESC).size();
    }

}
