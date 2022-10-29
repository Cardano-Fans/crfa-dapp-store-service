package crfa.app.jobs;

import crfa.app.service.ScriptHashesService;
import io.micronaut.scheduling.annotation.Scheduled;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Singleton
public class ScriptHashesJob {

    @Inject
    private ScriptHashesService scriptHashesService;

    @Scheduled(cron = "0 30 5 1/1 * ?") // every day at 5.30 AM
    public void scriptStatsJob() {
        log.info("script stats job...");
        scriptHashesService.ingestAll();
        log.info("script stats job finished.");
    }

}
