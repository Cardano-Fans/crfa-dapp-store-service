package crfa.app.service;


import crfa.app.client.crfa_db_sync_api.CRFADbSyncApi;
import crfa.app.domain.ScriptStats;
import crfa.app.repository.total.DappScriptsRepository;
import crfa.app.repository.total.ScriptHashesStatsRepository;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import reactor.core.publisher.Mono;

import java.util.ArrayList;

import static crfa.app.domain.ScriptStatsType.DB_SYNC;
import static crfa.app.domain.ScriptStatsType.SCROLLS;
import static crfa.app.domain.ScriptType.MINT;
import static crfa.app.domain.ScriptType.SPEND;

@Singleton
@Slf4j
public class ScriptHashesService {

    @Inject
    private ScrollsOnChainDataService scrollsOnChainDataService;

    @Inject
    private ScriptHashesStatsRepository scriptHashesStatsRepository;

    @Inject
    private DappScriptsRepository dappScriptsRepository;

    @Inject
    private CRFADbSyncApi crfaDbSyncApi;

    public void ingestAll() {
        log.info("Clearing db...");
        scriptHashesStatsRepository.clearDB();
        log.info("Cleared db.");

        processScrolls();
        processDbSync();
    }

    private void processScrolls() {
        log.info("loading scrolls scriptStats...");

        log.info("loading scriptHashes...");
        val scriptHashes = scrollsOnChainDataService.listSpendScriptHashes();
        log.info("script hashes count:{}", scriptHashes.size());

        val scriptHashesCount = scrollsOnChainDataService.spendTransactionsCount(scriptHashes);

        var dappScriptItems = dappScriptsRepository.listDappScriptItems();
        val count = dappScriptItems.size();

        if (count == 0) {
            log.warn("Job finished, empty scripts db!");
            return;
        }

        log.info("list release items count:{}", count);

        scriptHashesCount.entrySet().stream().filter(entry -> entry.getValue() > 5).forEach(entry -> {
            val key = entry.getKey();
            val value = entry.getValue();

            if (dappScriptItems.stream()
                    .filter(item -> item.getScriptType() == SPEND)
                    .filter(item -> item.getHash().equalsIgnoreCase(key)).findAny().isEmpty()) {

                var scriptStats = ScriptStats.builder()
                        .scriptHash(key)
                        .scriptType(SPEND)
                        .count(value)
                        .type(SCROLLS)
                        .build();

                scriptHashesStatsRepository.upsert(scriptStats);
            }
        });

        log.info("loading mintPolicyId hashes...");
        var mintPolicyIdHashes = scrollsOnChainDataService.listMintScriptHashes();
        log.info("mintPolicyId hashes count:{}", mintPolicyIdHashes.size());

        log.info("loading mintPolicyIdHashes counts...");
        var mintHashesCount = scrollsOnChainDataService.mintTransactionsCount(mintPolicyIdHashes);
        log.info("loading mintPolicyId hashes loaded.");

        log.info("upserting mint hashes to db...");

        mintHashesCount.entrySet().stream().filter(entry -> entry.getValue() > 5).forEach(entry -> {
            val key = entry.getKey();
            val value = entry.getValue();

            if (dappScriptItems.stream()
                    .filter(item -> item.getScriptType() == MINT)
                    .filter(item -> item.getHash().equalsIgnoreCase(key)).findAny().isEmpty()) {

                var scriptStats = ScriptStats.builder()
                        .scriptHash(key)
                        .scriptType(MINT)
                        .count(value)
                        .type(SCROLLS)
                        .build();

                scriptHashesStatsRepository.upsert(scriptStats);
            }
        });

        log.info("script hashes stats job completed.");
    }

    private void processDbSync() {
        val listReleaseItems = dappScriptsRepository.listDappScriptItems();
        val count = (long) listReleaseItems.size();

        if (count == 0) {
            log.warn("Job finished, empty script release items db!");
            return;
        }

        val topScriptsMap = Mono.from(crfaDbSyncApi.topScripts(5000)).block();

        val scriptStatsList = new ArrayList<ScriptStats>();

        topScriptsMap.entrySet().stream().filter(entry -> entry.getValue() > 5).forEach(entry -> {
            val key = entry.getKey();
            val value = entry.getValue();

            val foundIt = listReleaseItems.stream().filter(dAppReleaseItem -> dAppReleaseItem.getHash().contains(key)).findAny();

            if (foundIt.isEmpty()) {
                val scriptStats = ScriptStats.builder()
                        .scriptHash(key)
                        .scriptType(SPEND)
                        .count(value)
                        .type(DB_SYNC)
                        .build();

                scriptStatsList.add(scriptStats);

                scriptHashesStatsRepository.upsert(scriptStats);
            }
        });

        scriptHashesStatsRepository.removeAllExcept(scriptStatsList);
    }

//    public void ingestScrolls() {
//
//    }
//
//    public void ingestDbSync() {
//    }

}
