package crfa.app.service.processor;

import crfa.app.domain.DappFeed;
import crfa.app.domain.InjestionMode;

public interface FeedPostProcessor {

    void process(DappFeed dappFeed, InjestionMode injestionMode);

}
