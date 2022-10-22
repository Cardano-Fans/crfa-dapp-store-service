package crfa.app.resource;

import crfa.app.repository.GlobalCategoryStatsRepository;
import crfa.app.repository.GlobalStatsEpochRepository;
import crfa.app.repository.GlobalStatsRepository;
import crfa.app.resource.model.GlobalCategoryStatsResult;
import crfa.app.resource.model.GlobalStatsEpochResult;
import crfa.app.resource.model.GlobalStatsResult;
import io.micronaut.http.annotation.Controller;
import io.micronaut.http.annotation.Get;
import jakarta.inject.Inject;
import lombok.extern.slf4j.Slf4j;
import lombok.val;

import java.util.AbstractMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;

import static java.util.stream.Collectors.toMap;

@Controller("/global")
@Slf4j
public class GlobalResource {

    @Inject
    private GlobalStatsRepository globalStatsRepository;

    @Inject
    private GlobalStatsEpochRepository globalStatsEpochRepository;

    @Inject
    private GlobalCategoryStatsRepository globalCategoryStatsRepository;

    @Get(uri = "/stats", produces = "application/json")
    public Optional<GlobalStatsResult> globalStats() {
        return globalStatsRepository.findGlobalStats().map(globalStats -> {
            val b = GlobalStatsResult.builder();

            b.totalScriptsLocked(globalStats.getTotalScriptsLocked());
            b.trxCount(globalStats.getTotalTrxCount());
            b.volume(globalStats.getTotalVolume());
            b.totalDappsCount(globalStats.getTotalDapps());
            b.fees(globalStats.getTotalFees());
            b.avgFee(globalStats.getAvgFee());
            b.avgTrxSize(globalStats.getAvgTrxSize());

            b.totalUniqueAccounts(globalStats.getTotalUniqueAccounts());

            b.adaPriceEUR(globalStats.getAdaPriceEUR());
            b.adaPriceUSD(globalStats.getAdaPriceUSD());

            return b.build();
        });
    }

    @Get(uri = "/stats/epochs", produces = "application/json")
    public Map<Integer, GlobalStatsEpochResult> globalEpochStats() {
        return globalStatsEpochRepository.listGlobalStats().stream().map(globalStats -> {
            val b = GlobalStatsEpochResult.builder();

            b.inflowsOutflows(globalStats.getInflowsOutflows());
            b.trxCount(globalStats.getTotalTrxCount());
            b.volume(globalStats.getTotalVolume());
            b.fees(globalStats.getTotalFees());
            b.avgFee(globalStats.getAvgFee());
            b.avgTrxSize(globalStats.getAvgTrxSize());
            b.totalUniqueAccounts(globalStats.getTotalUniqueAccounts());

            return new AbstractMap.SimpleEntry<>(globalStats.getEpochNo(), b.build());
        }).collect(toMap(Map.Entry::getKey, Map.Entry::getValue, (e1, e2) -> e1, LinkedHashMap::new));
    }

    @Get(uri = "/stats/category", produces = "application/json")
    public Map<String, GlobalCategoryStatsResult> globalCategoryStats() {
        return globalCategoryStatsRepository.listGlobalStats().stream().map(globalStats -> {
            val b = GlobalCategoryStatsResult.builder();

            b.trxCount(globalStats.getTrxCount());
            b.volume(globalStats.getVolume());
            b.fees(globalStats.getFees());
            b.avgFee(globalStats.getAvgFee());
            b.scriptsLocked(globalStats.getScriptsLocked());
            b.avgTrxSize(globalStats.getAvgTrxSize());
            b.dapps(globalStats.getDapps());

            return new AbstractMap.SimpleEntry<>(globalStats.getCategoryType(), b.build());
        }).collect(toMap(AbstractMap.SimpleEntry::getKey, AbstractMap.SimpleEntry::getValue));
    }

}
