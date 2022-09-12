package crfa.app.service;

import crfa.app.domain.DappFeed;
import io.micronaut.context.ApplicationContext;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import lombok.extern.slf4j.Slf4j;
import lombok.val;

@Singleton
@Slf4j
public class DappIngestionService {

    @Inject
    private ApplicationContext appContext;

    public void process(DappFeed dappFeed) {
        val beans = appContext.getActiveBeanRegistrations(FeedProcessor.class);

        beans.forEach(b -> {
            log.info("Processing, dappFeed:{}", b.getName());
            b.getBean().process(dappFeed);
            log.info("Finished, dappFeed:{}", b.getName());
        });
    }

}
