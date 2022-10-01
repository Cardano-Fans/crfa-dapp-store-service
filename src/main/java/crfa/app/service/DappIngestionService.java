package crfa.app.service;

import crfa.app.domain.DappFeed;
import crfa.app.domain.InjestionMode;
import crfa.app.service.processor.FeedProcessor;
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

    public void process(DappFeed dappFeed, InjestionMode injestionMode) {
        val beans = appContext.getActiveBeanRegistrations(FeedProcessor.class);

        beans.forEach(bean -> {
            log.info("Processing, dappFeed:{}", bean.getName());
            bean.getBean().process(dappFeed, injestionMode);
            log.info("Finished, dappFeed:{}", bean.getName());
        });
    }

}
