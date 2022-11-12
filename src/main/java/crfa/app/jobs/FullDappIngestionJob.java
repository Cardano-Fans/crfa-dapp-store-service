package crfa.app.jobs;

import crfa.app.domain.InjestionMode;
import crfa.app.service.DappFeedCreator;
import crfa.app.service.DappIngestionService;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import lombok.extern.slf4j.Slf4j;
import lombok.val;

@Singleton
@Slf4j
public class FullDappIngestionJob {

    @Inject
    private DappIngestionService dappIngestionService;

    @Inject
    private DappFeedCreator dappFeedCreator;

    //@Scheduled(cron = "0 30 4 1/1 * ?") // every day at 4.30 AM
    public void onScheduled() {
        val injestionMode = InjestionMode.FULL;

        log.info("Dapps update scheduled, mode:{}", injestionMode);

        log.info("Gathering data feed...");
        val dataFeed = dappFeedCreator.createFeed(injestionMode);
        log.info("Got data feed.");

        dappIngestionService.process(dataFeed, injestionMode);

        log.info("Dapps update completed, mode:{}", injestionMode);
    }

}
