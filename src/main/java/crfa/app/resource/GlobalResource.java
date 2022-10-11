package crfa.app.resource;

import crfa.app.domain.Global;
import crfa.app.repository.GlobalStatsRepository;
import io.micronaut.http.annotation.Controller;
import io.micronaut.http.annotation.Get;
import jakarta.inject.Inject;
import lombok.extern.slf4j.Slf4j;
import lombok.val;

import java.util.Optional;

@Controller("/global")
@Slf4j
public class GlobalResource {

    @Inject
    private GlobalStatsRepository globalStatsRepository;

    @Get(uri = "/stats", produces = "application/json")
    public Optional<Global> global() {
        return globalStatsRepository.returnGlobalStats().map(globalStats -> {
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

}
