package crfa.app.jobs;

import crfa.app.service.DappService;
import io.micronaut.scheduling.annotation.Scheduled;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import lombok.extern.slf4j.Slf4j;

@Singleton
@Slf4j
public class DappIngestionJob {

    @Inject
    private DappService dappService;

    @Scheduled(fixedDelay = "6h", initialDelay = "1m")
    public void onScheduled() {
        log.info("Dapps update scheduled.");

        log.info("Gathering data feed...");
        var dataFeed = dappService.gatherDappDataFeed();
        log.info("Got data feed.");

        log.info("Upserting dapp releases...");
        dappService.upsertDappReleases(dataFeed);
        log.info("Upserted dapp releases.");

        log.info("Upserting dapp release items...");
        dappService.upsertDappReleaseItems(dataFeed);
        log.info("Upserted dapp release items.");

        log.info("Dapps update completed.");
    }

}
