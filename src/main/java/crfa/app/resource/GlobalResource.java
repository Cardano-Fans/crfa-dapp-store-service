package crfa.app.resource;

import crfa.app.domain.AdaPricePerDay;
import crfa.app.domain.Global;
import crfa.app.repository.total.AdaPriceRepository;
import crfa.app.repository.total.DappsRepository;
import io.micronaut.http.annotation.Controller;
import io.micronaut.http.annotation.Get;
import jakarta.inject.Inject;
import lombok.extern.slf4j.Slf4j;
import lombok.val;

import java.math.BigDecimal;
import java.util.Optional;

import static crfa.app.domain.DappAggrType.ALL;
import static crfa.app.domain.SortBy.SCRIPTS_INVOKED;
import static crfa.app.domain.SortOrder.ASC;

@Controller("/global")
@Slf4j
public class GlobalResource {

    @Inject
    private DappsRepository dappsRepository;

    @Inject
    private AdaPriceRepository adaPriceRepository;

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
        builder.trxCount(dappsRepository.totalScriptInvocations());
        builder.volume(dappsRepository.volume());

        try {
            val dappUniqueReleases = dappsRepository.listDapps(Optional.of(SCRIPTS_INVOKED), Optional.of(ASC), ALL).stream().count();
            builder.totalDappsCount(dappUniqueReleases);
        } catch (InvalidParameterException e) {
            throw new RuntimeException(e);
        }

        return builder.build();
    }

}
