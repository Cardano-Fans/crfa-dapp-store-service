package crfa.app.jobs;

import crfa.app.repository.epoch.DappReleaseEpochRepository;
import crfa.app.repository.epoch.DappScriptsEpochRepository;
import crfa.app.repository.epoch.DappsEpochRepository;
import crfa.app.service.DappService;
import io.micronaut.scheduling.annotation.Scheduled;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import lombok.extern.slf4j.Slf4j;
import lombok.val;

@Singleton
@Slf4j
public class EpochCloserJob {

    @Inject
    private DappReleaseEpochRepository dappReleaseEpochRepository;

    @Inject
    private DappScriptsEpochRepository dappScriptsEpochRepository;

    @Inject
    private DappsEpochRepository dappsEpochRepository;

    @Inject
    private DappService dappService;

    //@Scheduled(fixedDelay = "15m", initialDelay = "1m")
    @Scheduled(cron = "0 0 0 1/1 * ?") // run every day at midnight
    public void onScheduled() {
        log.info("Executing epoch closer...");
        val epochNo = dappService.currentEpoch();

        dappReleaseEpochRepository.closeEpochs(epochNo);
        dappScriptsEpochRepository.closeEpochs(epochNo);
        dappsEpochRepository.closeEpochs(epochNo);

        log.info("Executing epoch closer done.");
    }

}
