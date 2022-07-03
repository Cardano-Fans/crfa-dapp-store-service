package crfa.app.resource;

import crfa.app.domain.AdaPricePerDay;
import crfa.app.domain.DAppRelease;
import crfa.app.domain.Global;
import crfa.app.repository.AdaPriceRepository;
import crfa.app.repository.DappReleasesRepository;
import io.micronaut.http.annotation.Controller;
import io.micronaut.http.annotation.Get;
import jakarta.inject.Inject;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RedissonClient;

import java.math.BigDecimal;
import java.util.stream.Collectors;

import static java.util.stream.Collectors.groupingBy;

@Controller("/global")
@Slf4j
public class GlobalResource {

    @Inject
    private DappReleasesRepository dappReleasesRepository;

    @Inject
    private AdaPriceRepository adaPriceRepository;

    @Inject
    private RedissonClient redissonClient;

    @Get(uri = "/stats", produces = "application/json")
    public Global global() {
        Global.GlobalBuilder builder = Global.builder();

        builder.adaPriceEUR(adaPriceRepository.getLatestPrice("EUR")
                .map(AdaPricePerDay::getPrice)
                .map(BigDecimal::valueOf)
                .orElse(null));
        builder.adaPriceUSD(adaPriceRepository.getLatestPrice("USD")
                .map(AdaPricePerDay::getPrice)
                .map(BigDecimal::valueOf)
                .orElse(null));
        builder.totalScriptsLocked(dappReleasesRepository.totalScriptsLocked());
        builder.totalSmartContractsTransactionCount(dappReleasesRepository.totalContractTransactionsCount());
        builder.totalScriptInvocationsCount(dappReleasesRepository.totalScriptInvocations());

        var dappUniqueReleases = dappReleasesRepository.dappUniqueReleases();

        builder.totalDappsCount(dappUniqueReleases.size());
        builder.countDappsByDappType(dappUniqueReleases.stream().collect(groupingBy(DAppRelease::getDAppType, Collectors.counting())));

        return builder.build();
    }

}
