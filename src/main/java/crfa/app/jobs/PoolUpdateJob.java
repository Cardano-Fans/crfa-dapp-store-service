package crfa.app.jobs;

import crfa.app.service.PoolService;
import io.micronaut.scheduling.annotation.Scheduled;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import lombok.extern.slf4j.Slf4j;

@Singleton
@Slf4j
public class PoolUpdateJob {

    @Inject
    private PoolService poolService;

    @Scheduled(cron = "0 30 6 1/1 * ?") // every day at 6.30 AM
    public void updatePools() {
        log.info("pools update job...");
        poolService.updatePools();
        log.info("pools update job completed.");
    }

}
