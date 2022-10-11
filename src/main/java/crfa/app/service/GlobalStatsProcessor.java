package crfa.app.service;

import crfa.app.domain.*;
import crfa.app.repository.GlobalStatsRepository;
import crfa.app.repository.total.AdaPriceRepository;
import crfa.app.repository.total.DappsRepository;
import crfa.app.resource.InvalidParameterException;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import lombok.extern.slf4j.Slf4j;
import lombok.val;

import java.math.BigDecimal;
import java.util.Date;
import java.util.Optional;

import static crfa.app.domain.DappAggrType.ALL;
import static crfa.app.domain.SortBy.SCRIPTS_INVOKED;
import static crfa.app.domain.SortOrder.ASC;

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
        try {
            return dappsRepository.listDapps(Optional.of(SCRIPTS_INVOKED), Optional.of(ASC), ALL).size();
        } catch (InvalidParameterException e) {
            throw new RuntimeException(e);
        }
    }

}
