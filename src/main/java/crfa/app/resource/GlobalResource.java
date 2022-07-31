package crfa.app.resource;

import crfa.app.domain.AdaPricePerDay;
import crfa.app.domain.Global;
import crfa.app.repository.AdaPriceRepository;
import crfa.app.repository.DappsRepository;
import crfa.app.service.DappService;
import io.micronaut.http.annotation.Controller;
import io.micronaut.http.annotation.Get;
import jakarta.inject.Inject;
import lombok.extern.slf4j.Slf4j;

import java.math.BigDecimal;
import java.util.Optional;

import static crfa.app.domain.SortBy.SCRIPTS_INVOKED;
import static crfa.app.domain.SortOrder.ASC;

@Controller("/global")
@Slf4j
public class GlobalResource {

    @Inject
    private DappService dappService;

    @Inject
    private DappsRepository dappsRepository;

    @Inject
    private AdaPriceRepository adaPriceRepository;

//    @Inject
//    private RedissonClient redissonClient;

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

        builder.totalScriptsLocked(dappsRepository.totalScriptsLocked());
        builder.totalSmartContractsTransactionCount(dappsRepository.totalContractTransactionsCount());
        builder.totalScriptInvocationsCount(dappsRepository.totalScriptInvocations());

        try {
            var dappUniqueReleases = dappsRepository.listDapps(Optional.of(SCRIPTS_INVOKED), Optional.of(ASC)).stream().count();
            builder.totalDappsCount(dappUniqueReleases);
        } catch (InvalidParameterException e) {
            throw new RuntimeException(e);
        }

        return builder.build();
    }

}
