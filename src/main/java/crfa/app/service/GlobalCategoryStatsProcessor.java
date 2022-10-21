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

            b.categoryType(cat);
            b.updateTime(new Date());
            b.avgFee(dappPerCat.stream().filter(Objects::nonNull).mapToDouble(DApp::getAvgFee).average().orElse(0.0D));
            b.scriptsLocked(dappPerCat.stream().filter(Objects::nonNull).mapToLong(DApp::getScriptsLocked).sum());
            b.trxCount(dappPerCat.stream().filter(Objects::nonNull).mapToLong(DApp::getScriptInvocationsCount).sum());
            b.fees(dappPerCat.stream().filter(Objects::nonNull).mapToLong(DApp::getFees).sum());
            b.volume(dappPerCat.stream().filter(Objects::nonNull).mapToLong(DApp::getVolume).sum());
            b.dapps(dappPerCat.size());

            globalStatsRepository.upsert(b.build());
        }

    }

}
