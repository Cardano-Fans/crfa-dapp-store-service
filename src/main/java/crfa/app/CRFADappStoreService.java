package crfa.app;

import io.micronaut.runtime.Micronaut;
import io.micronaut.runtime.event.ApplicationShutdownEvent;
import io.micronaut.runtime.event.annotation.EventListener;
import io.micronaut.runtime.server.event.ServerStartupEvent;
import jakarta.inject.Singleton;
import lombok.extern.slf4j.Slf4j;

@Singleton
@Slf4j
public class CRFADappStoreService {

    public static void main(String[] args) {
        Micronaut.build(args)
                .eagerInitSingletons(true)
                .mainClass(CRFADappStoreService.class)
                .banner(false)
                .start();
    }

    @EventListener
    public void onStartup(ServerStartupEvent event) {
        log.info("Starting CRFADappStoreService...");
    }

    @EventListener
    public void stop(final ApplicationShutdownEvent event) {
        log.info("Stopping CRFADappStoreService...");
        log.info("CRFADappStoreService stopped.");
    }

}
