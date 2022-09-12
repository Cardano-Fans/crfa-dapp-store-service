package crfa.app.jobs;

import crfa.app.client.crfa_db_sync_api.CRFADbSyncApi;
import crfa.app.domain.ScriptStats;
import crfa.app.domain.ScriptStatsType;
import crfa.app.repository.DappReleaseItemRepository;
import crfa.app.repository.ScriptHashesStatsRepository;
import crfa.app.service.ScrollsOnChainDataService;
import io.micronaut.scheduling.annotation.Scheduled;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import reactor.core.publisher.Mono;

import static crfa.app.domain.ScriptType.SPEND;

@Slf4j
@Singleton
public class ScriptHashesJob {

    @Inject
    private ScrollsOnChainDataService scrollsOnChainDataService;

    @Inject
    private ScriptHashesStatsRepository scriptHashesStatsRepository;

    @Inject
    private DappReleaseItemRepository releaseItemsRepository;

    @Inject
    private CRFADbSyncApi crfaDbSyncApi;

    @Scheduled(fixedDelay = "1h", initialDelay = "5m")
    public void scriptStatsJob() {
        log.info("Clearing db...");
        scriptHashesStatsRepository.clearDB();
        log.info("Cleared db.");

        //processScrolls();
        processDbSync();
    }

//    private void processScrolls() {
//        log.info("loading scriptStats...");
//
//        log.info("loading scriptHashes...");
//        var scriptHashes = scrollsOnChainDataService.listScriptHashes();
//        log.info("script hashes count:{}", scriptHashes.size());
//
//        log.info("loading scriptHashes counts...");
//        var scriptHashesCount = scrollsOnChainDataService.scriptHashesCount(scriptHashes, true);
//
//        var listReleaseItems = releaseItemsRepository.listReleaseItems();
//        final var count = (long) listReleaseItems.size();
//
//        if (count == 0) {
//            log.warn("Job finished, empty script release items db!");
//            return;
//        }
//
//        log.info("list release items count:{}", count);
//
//        scriptHashesCount.forEach((key, value) -> {
//            if (listReleaseItems.stream()
//                    .filter(item -> item.getScriptType() == SPEND)
//                    .filter(item -> item.getHash().equalsIgnoreCase(key)).findAny().isEmpty()) {
//
//                var scriptStats = ScriptStats.builder()
//                        .scriptHash(key)
//                        .scriptType(SPEND)
//                        .count(value)
//                        .type(ScriptStatsType.SCROLLS)
//                        .build();
//
//                //log.info("script hash:{} missing, inserting:{}", key, scriptStats);
//
//                scriptHashesStatsRepository.upsert(scriptStats);
//            }
//        });

//        log.info("loading mintPolicyIdHashes...");
//        var mintPolicyIdHashes = scrollsOnChainDataService.listMintHashes();
//        log.info("mintPolicyIdHashes count:{}", mintPolicyIdHashes.size());
//
//        log.info("loading mintPolicyIdHashes counts...");
//        var mintHashesCount = scrollsOnChainDataService.mintScriptsCount(mintPolicyIdHashes);
//
//        log.info("upserting script hashes to db...");
//
//        log.info("upserting mint hashes to db...");
//
//        mintHashesCount.forEach((key, value) -> {
//            if (listReleaseItems.stream()
//                    .filter(item -> item.getScriptType() == MINT)
//                    .filter(item -> item.getHash().equalsIgnoreCase(key)).findAny().isEmpty()) {
//
//                var scriptStats = ScriptStats.builder()
//                        .scriptHash(key)
//                        .scriptType(MINT)
//                        .transactionCount(value)
//                        .build();
//
//                log.info("script mintPolicyId:{} missing, inserting:{}", key, scriptStats);
//
//                scriptHashesStatsRepository.upsert(scriptStats);
//            }
//        });
//
//        log.info("script hashes stats job completed.");
//    }

    private void processDbSync() {
        val listReleaseItems = releaseItemsRepository.listReleaseItems();
        val count = (long) listReleaseItems.size();

        if (count == 0) {
            log.warn("Job finished, empty script release items db!");
            return;
        }

        val topScriptsMap = Mono.from(crfaDbSyncApi.topScripts(5000)).block();

        topScriptsMap.forEach((key, scriptInvocations) -> {
            val foundIt = listReleaseItems.stream().filter(dAppReleaseItem -> dAppReleaseItem.getHash().contains(key)).findAny();

            if (foundIt.isEmpty()) {
                val scriptStats = ScriptStats.builder()
                        .scriptHash(key)
                        .scriptType(SPEND)
                        .count(scriptInvocations)
                        .type(ScriptStatsType.DB_SYNC)
                        .build();

                scriptHashesStatsRepository.upsert(scriptStats);
            }
        });
    }

}
