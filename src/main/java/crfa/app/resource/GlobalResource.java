package crfa.app.resource;

import crfa.app.domain.AdaPricePerDay;
import crfa.app.domain.Global;
import crfa.app.repository.AdaPriceRepository;
import crfa.app.repository.DappReleasesRepository;
import io.micronaut.http.annotation.Controller;
import io.micronaut.http.annotation.Get;
import jakarta.inject.Inject;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RedissonClient;

import java.math.BigDecimal;

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
    public Global global() throws Exception {

        var transactionsCount = redissonClient
                .getAtomicLong("total_transactions_count");

//        var totalSmartContractTransactions = redissonClient
//                .getAtomicLong("total_transactions_count_by_contract_addresses");

        return Global.builder()
                .adaPriceEUR(adaPriceRepository.getLatestPrice("EUR")
                        .map(AdaPricePerDay::getPrice)
                        .map(BigDecimal::valueOf)
                        .orElse(null))
                .adaPriceUSD(adaPriceRepository.getLatestPrice("USD")
                        .map(AdaPricePerDay::getPrice)
                        .map(BigDecimal::valueOf)
                        .orElse(null))
//                .totalSmartContractsTransactionCount(totalSmartContractTransactions.get())
                .totalScriptInvocationsCount(dappReleasesRepository.totalScriptInvocations())
                .totalTVL(dappReleasesRepository.scriptsLocked())
                .totalTransactionsCount(transactionsCount.get())
                .build();
    }

}
