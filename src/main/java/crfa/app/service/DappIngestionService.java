package crfa.app.service;

import crfa.app.domain.DappFeed;
import crfa.app.domain.FeedProcessingContext;
import crfa.app.domain.InjestionMode;
import crfa.app.service.processor.FeedProcessor;
import io.micronaut.context.ApplicationContext;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import lombok.extern.slf4j.Slf4j;
import lombok.val;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

import static crfa.app.domain.InjestionMode.WITHOUT_EPOCHS_ONLY_AGGREGATES;

@Singleton
@Slf4j
public class DappIngestionService {

    @Inject
    private ApplicationContext appContext;

    @Inject
    private GlobalStatsProcessor globalStatsProcessor;

    @Inject
    private GlobalStatsEpochProcessor globalStatsEpochProcessor;

    @Inject
    private GlobalCategoryStatsProcessor globalCategoryStatsProcessor;

    public void process(DappFeed dappFeed, InjestionMode injestionMode) {
        val beans = appContext.getActiveBeanRegistrations(FeedProcessor.class);

//        val uniqueAccounts = BloomFilter.<String>create(
//                Funnels.stringFunnel(StandardCharsets.UTF_8),
//                200_000,
//                0.01);

        val totalUniqueAccounts = new HashSet<String>();
        val totalUniqueAccountsEpoch = new HashMap<Integer, Set<String>>();

        val context = FeedProcessingContext.builder()
                .uniqueAccounts(totalUniqueAccounts)
                .uniqueAccountsEpoch(totalUniqueAccountsEpoch)
                .build();

        for (val bean : beans) {
            val b = bean.getBean();

            log.info("Processing, dappFeed:{}", bean.getName());

//            if (b.isEpochProcessor()) {
//                continue;
//            }

            if (b.isEpochProcessor() && injestionMode == WITHOUT_EPOCHS_ONLY_AGGREGATES) {
                log.info("This bean is epoch processor bean and ingestionMode:{}, continue to the next one!", injestionMode);
                continue;
            }

            bean.getBean().process(dappFeed, injestionMode, context);

            log.info("Finished, dappFeed:{}", bean.getName());
        }

        log.info("global stats processor...");
        globalStatsProcessor.process(dappFeed, injestionMode, context);

        if (injestionMode != WITHOUT_EPOCHS_ONLY_AGGREGATES) {
            globalStatsEpochProcessor.process(dappFeed, injestionMode, context);
        }
        log.info("global stats processor done.");

        log.info("global category stats processor...");
        globalCategoryStatsProcessor.process(dappFeed, injestionMode, context);
        log.info("global category stats processor done.");
    }

}
