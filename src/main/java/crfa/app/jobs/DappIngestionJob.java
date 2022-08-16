package crfa.app.jobs;

import crfa.app.service.DappFeedCreator;
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

    @Inject
    private DappFeedCreator dappFeedCreator;

    @Scheduled(fixedDelay = "15m", initialDelay = "5s")
    public void onScheduled() {
        log.info("Dapps update scheduled.");

        log.info("Gathering data feed...");
        var dataFeed = dappFeedCreator.createFeed();
        log.info("Got data feed.");

        dappIngestionService.process(dataFeed);

        log.info("Dapps update completed.");
    }

}
