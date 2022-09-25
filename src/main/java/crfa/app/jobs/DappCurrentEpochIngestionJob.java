package crfa.app.jobs;

import crfa.app.domain.InjestionMode;
import crfa.app.service.DappFeedCreator;
import crfa.app.service.DappIngestionService;
import io.micronaut.scheduling.annotation.Scheduled;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import lombok.extern.slf4j.Slf4j;
import lombok.val;

@Singleton
@Slf4j
public class DappCurrentEpochIngestionJob {

    @Inject
    private DappIngestionService dappIngestionService;

    @Inject
    private DappFeedCreator dappFeedCreator;

    @Scheduled(fixedDelay = "5m", initialDelay = "10m")
    public void onScheduled() {
        val injestionMode = InjestionMode.CURRENT_EPOCH;

        log.info("Dapps update scheduled, mode:{}", injestionMode);

        log.info("Gathering data feed...");
        val dataFeed = dappFeedCreator.createFeed(injestionMode);
        log.info("Got data feed.");

        dappIngestionService.process(dataFeed, injestionMode);

        log.info("Dapps update completed, mode:{}", injestionMode);
    }

}
