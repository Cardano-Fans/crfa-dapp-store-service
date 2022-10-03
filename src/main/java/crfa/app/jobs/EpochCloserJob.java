package crfa.app.jobs;

import crfa.app.repository.epoch.DappReleaseEpochRepository;
import crfa.app.repository.epoch.DappScriptsEpochRepository;
import crfa.app.repository.epoch.DappsEpochRepository;
import crfa.app.service.ScrollsOnChainDataService;
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
    private ScrollsOnChainDataService scrollsOnChainDataService;

    @Scheduled(fixedDelay = "5m", initialDelay = "15m")
    public void onScheduled() {
        log.info("Executing epoch closer...");
        val epochNo = scrollsOnChainDataService.currentEpoch().get().intValue();

        dappReleaseEpochRepository.closeEpochs(epochNo);
        dappScriptsEpochRepository.closeEpochs(epochNo);
        dappsEpochRepository.closeEpochs(epochNo);
        log.info("Executing epoch closer done.");
    }

}
