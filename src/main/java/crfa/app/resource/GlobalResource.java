package crfa.app.resource;

import crfa.app.repository.GlobalCategoryStatsEpochRepository;
import crfa.app.repository.GlobalCategoryStatsRepository;
import crfa.app.repository.GlobalStatsEpochRepository;
import crfa.app.repository.GlobalStatsRepository;
import crfa.app.resource.model.GlobalCategoryEpochStatsResult;
import crfa.app.resource.model.GlobalCategoryStatsResult;
import crfa.app.resource.model.GlobalStatsEpochResult;
import crfa.app.resource.model.GlobalStatsResult;
import io.micronaut.core.annotation.Blocking;
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

    @Inject
    private GlobalCategoryStatsEpochRepository globalCategoryStatsEpochRepository;

    @Get(uri = "/stats", produces = "application/json")
    @Blocking
    public Optional<GlobalStatsResult> globalStats() {
        return globalStatsRepository.findGlobalStats().map(globalStats -> {
            val b = GlobalStatsResult.builder();

            b.totalScriptsLocked(globalStats.getBalance());
            b.balance(globalStats.getBalance());
            b.trxCount(globalStats.getSpendTransactions() + globalStats.getMintTransactions());
            b.volume(globalStats.getSpendVolume() + 0);
            b.totalDappsCount(globalStats.getDapps());
            b.trxFees(globalStats.getSpendTrxFees() + 0);
            b.avgTrxFee(globalStats.getAvgTrxFee());
            b.avgTrxSize(globalStats.getAvgTrxSize());
            b.totalUniqueAccounts(globalStats.getSpendUniqueAccounts() + 0);

            b.adaPriceEUR(globalStats.getAdaPriceEUR());
            b.adaPriceUSD(globalStats.getAdaPriceUSD());

            return b.build();
        });
    }

    @Get(uri = "/stats/epochs", produces = "application/json")
    @Blocking
    public Map<Integer, GlobalStatsEpochResult> globalEpochStats() {
        return globalStatsEpochRepository.listGlobalStats().stream().map(globalStats -> {
            val b = GlobalStatsEpochResult.builder();

            b.trxCount(globalStats.getTransactionsCount());
            b.inflowsOutflows(globalStats.getInflowsOutflows());
            b.volume(globalStats.getSpendVolume() + 0);
            b.fees(globalStats.getSpendTrxFees() + 0);
            b.avgTrxFee(globalStats.getAvgTrxFee());
            b.avgTrxSize(globalStats.getAvgTrxSize());
            b.totalUniqueAccounts(globalStats.getSpendUniqueAccounts() + 0);

            return new AbstractMap.SimpleEntry<>(globalStats.getEpochNo(), b.build());
        }).collect(toMap(Map.Entry::getKey, Map.Entry::getValue, (e1, e2) -> e1, LinkedHashMap::new));
    }

    @Get(uri = "/stats/category", produces = "application/json")
    @Blocking
    public Map<String, GlobalCategoryStatsResult> globalCategoryStats() {
        return globalCategoryStatsRepository.list().stream().map(globalStats -> {
            val b = GlobalCategoryStatsResult.builder();

            b.balance(globalStats.getBalance());
            b.trxCount(globalStats.getTransactionsCount());
            b.volume(globalStats.getSpendVolume() + 0);
            b.fees(globalStats.getSpendTrxFees() + 0);
            b.avgTrxFee(globalStats.getAvgTrxFee());
            b.avgTrxSize(globalStats.getAvgTrxSize());
            b.uniqueAccounts(globalStats.getSpendUniqueAccounts() + 0);

            b.dapps(globalStats.getDapps());

            return new AbstractMap.SimpleEntry<>(globalStats.getCategoryType(), b.build());
        }).collect(toMap(AbstractMap.SimpleEntry::getKey, AbstractMap.SimpleEntry::getValue));
    }

    @Get(uri = "/stats/category/epochs", produces = "application/json")
    @Blocking
    public Map<String, GlobalCategoryEpochStatsResult> globalCategoryStatsEpochs() {

        return globalCategoryStatsEpochRepository.list().stream().map(globalCategoryStats -> {
            val b = GlobalCategoryEpochStatsResult.builder();

            b.id(globalCategoryStats.getId());
            b.epochNo(globalCategoryStats.getEpochNo());
            b.categoryType(globalCategoryStats.getCategoryType());
            b.inflowsOutflows(globalCategoryStats.getInflowsOutflows());
            b.trxCount(globalCategoryStats.getTransactionsCount());
            b.volume(globalCategoryStats.getSpendVolume() + 0);
            b.fees(globalCategoryStats.getSpendTrxFees() + 0);
            b.avgTrxFee(globalCategoryStats.getAvgTrxFee());
            b.avgTrxSize(globalCategoryStats.getAvgTrxSize());
            b.uniqueAccounts(globalCategoryStats.getSpendUniqueAccounts() + 0);
            b.dapps(globalCategoryStats.getDapps());

            //TODO unique accounts

            return new AbstractMap.SimpleEntry<>(globalCategoryStats.getId(), b.build());
        }).collect(toMap(Map.Entry::getKey, Map.Entry::getValue, (e1, e2) -> e1, LinkedHashMap::new));
    }

}
