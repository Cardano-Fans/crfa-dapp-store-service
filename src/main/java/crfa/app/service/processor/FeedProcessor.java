package crfa.app.service.processor;

import crfa.app.domain.DappFeed;
import crfa.app.domain.InjestionMode;

public interface FeedProcessor {

    void process(DappFeed dappFeed, InjestionMode injestionMode);

}
