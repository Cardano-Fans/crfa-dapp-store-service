package crfa.app.resource;

import crfa.app.domain.Global;
import crfa.app.domain.GlobalEpoch;
import crfa.app.repository.GlobalStatsEpochRepository;
import crfa.app.repository.GlobalStatsRepository;
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

    @Get(uri = "/stats", produces = "application/json")
    public Optional<Global> global() {
        return globalStatsRepository.findGlobalStats().map(globalStats -> {
            val builder = Global.builder();

            builder.totalScriptsLocked(globalStats.getTotalScriptsLocked());
            builder.trxCount(globalStats.getTotalTrxCount());
            builder.volume(globalStats.getTotalVolume());
            builder.totalDappsCount(globalStats.getTotalDapps());
            builder.adaPriceEUR(globalStats.getAdaPriceEUR());
            builder.adaPriceUSD(globalStats.getAdaPriceUSD());
            builder.totalUniqueAccounts(globalStats.getTotalUniqueAccounts());

            return builder.build();
        });
    }

    @Get(uri = "/stats/epochs", produces = "application/json")
    public Map<Integer, GlobalEpoch> globalEpoch() {
        return globalStatsEpochRepository.listGlobalStats().stream().map(globalStats -> {
            val b = GlobalEpoch.builder();

            b.inflowsOutflows(globalStats.getInflowsOutflows());
            b.trxCount(globalStats.getTotalTrxCount());
            b.volume(globalStats.getTotalVolume());
            b.totalUniqueAccounts(globalStats.getTotalUniqueAccounts());

            return new AbstractMap.SimpleEntry<>(globalStats.getEpochNo(), b.build());
        }).collect(toMap(Map.Entry::getKey, Map.Entry::getValue, (e1, e2) -> e1, LinkedHashMap::new));
    }

}
