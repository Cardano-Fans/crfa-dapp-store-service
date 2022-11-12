package crfa.app.service;

import crfa.app.domain.DappFeed;
import crfa.app.domain.InjestionMode;
import crfa.app.service.processor.FeedPostProcessor;
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
//        val uniqueAccounts = BloomFilter.<String>create(
//                Funnels.stringFunnel(StandardCharsets.UTF_8),
//                200_000,
//                0.01);

        invokeFeedProcessors(dappFeed, injestionMode);
        invokeFeedPostProcessors(dappFeed, injestionMode);
    }

    private void invokeFeedProcessors(DappFeed dappFeed, InjestionMode injestionMode) {
        val feeProcessors = appContext.getActiveBeanRegistrations(FeedProcessor.class);
        log.info("processing feed processors, count:{}...", feeProcessors.size());
        for (val feedProcessorBean : feeProcessors) {
            val feedProcessor = feedProcessorBean.getBean();

            log.info("Processing, feedProcessor:{}", feedProcessorBean.getName());

            feedProcessor.process(dappFeed, injestionMode);

            log.info("Finished, feedProcessor:{}", feedProcessorBean.getName());
        }
    }

    private void invokeFeedPostProcessors(DappFeed dappFeed, InjestionMode injestionMode) {
        val feedPostProcessors = appContext.getActiveBeanRegistrations(FeedPostProcessor.class);

        log.info("processing post feed processors, count:{}...", feedPostProcessors.size());

        for (val feedProcessorBean : feedPostProcessors) {
            val feedPostProcessor = feedProcessorBean.getBean();

            log.info("Processing, feedPostProcessor:{}", feedProcessorBean.getName());

            feedPostProcessor.process(dappFeed, injestionMode);

            log.info("Finished, feedPostProcessor:{}", feedProcessorBean.getName());
        }
    }

}
