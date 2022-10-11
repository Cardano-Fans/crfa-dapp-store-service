package crfa.app.service;

import crfa.app.domain.DappFeed;
import crfa.app.domain.InjestionMode;
import crfa.app.service.processor.FeedProcessor;
import io.micronaut.context.ApplicationContext;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import lombok.extern.slf4j.Slf4j;
import lombok.val;

import static crfa.app.domain.InjestionMode.WITHOUT_EPOCHS_ONLY_AGGREGATES;

@Singleton
@Slf4j
public class DappIngestionService {

    @Inject
    private ApplicationContext appContext;

    public void process(DappFeed dappFeed, InjestionMode injestionMode) {
        val beans = appContext.getActiveBeanRegistrations(FeedProcessor.class);

        for (val bean : beans) {
            val b = bean.getBean();

            log.info("Processing, dappFeed:{}", bean.getName());

            if (b.isEpochProcessor() && injestionMode == WITHOUT_EPOCHS_ONLY_AGGREGATES) {
                log.info("This bean is epoch processor bean and ingestionMode:{}, continue to the next one!", injestionMode);
                continue;
            }

            bean.getBean().process(dappFeed, injestionMode);

            log.info("Finished, dappFeed:{}", bean.getName());
        }
    }

}
