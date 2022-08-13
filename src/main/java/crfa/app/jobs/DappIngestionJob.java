package crfa.app.jobs;

import crfa.app.service.DappIngestionService;
import io.micronaut.scheduling.annotation.Scheduled;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import lombok.extern.slf4j.Slf4j;

@Singleton
@Slf4j
public class DappIngestionJob {

    @Inject
    private DappIngestionService dappIngestionService;

    @Scheduled(fixedDelay = "1h", initialDelay = "5s")
    public void onScheduled() {
        log.info("Dapps update scheduled.");

        log.info("Gathering data feed...");
        var dataFeed = dappIngestionService.gatherDappDataFeed();
        log.info("Got data feed.");

        log.info("Upserting dapp releases...");
        dappIngestionService.upsertDappReleases(dataFeed);
        log.info("Upserted dapp releases.");

        log.info("Upserting dapp release items...");
        dappIngestionService.upsertDappReleaseItems(dataFeed);
        log.info("Upserted dapp release items.");

        log.info("Upserting dapps...");
        dappIngestionService.upsertDapps(dataFeed);
        log.info("Upserted dapps.");

        log.info("Dapps update completed.");
    }

}
