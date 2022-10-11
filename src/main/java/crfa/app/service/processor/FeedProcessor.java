package crfa.app.service.processor;

import crfa.app.domain.DappFeed;
import crfa.app.domain.FeedProcessingContext;
import crfa.app.domain.InjestionMode;

public interface FeedProcessor {

    default boolean isEpochProcessor() {
        return false;
    }

    void process(DappFeed dappFeed, InjestionMode injestionMode, FeedProcessingContext context);

}
