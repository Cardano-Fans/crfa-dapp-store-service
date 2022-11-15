package crfa.app.service.processor.pre;

import crfa.app.domain.DappFeed;
import crfa.app.domain.InjestionMode;
import crfa.app.service.ScrollsOnChainDataService;
import crfa.app.service.processor.FeedPreProcessor;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import lombok.extern.slf4j.Slf4j;
import lombok.val;

import java.util.HashSet;

import static crfa.app.domain.Purpose.SPEND;

@Singleton
@Slf4j
public class GlobalUniqueAccountsFeedPreProcessor implements FeedPreProcessor {

    @Inject
    private ScrollsOnChainDataService scrollsOnChainDataService;

    @Override
    public void process(DappFeed dappFeed, InjestionMode injestionMode) {
        val hashes = new HashSet<String>();

        for (val dsi : dappFeed.getDappSearchResult()) {
            for (val r : dsi.getReleases()) {
                for (val s : r.getScripts()) {
                    if (s.getPurpose() == SPEND) {
                        hashes.add(s.getUnifiedHash());
                    }
                }
            }
        }

        log.info("Storing global unique accounts set into redis...");
        val size = scrollsOnChainDataService.storeGlobalUniqueAccounts(hashes);

        log.info("Stored global unique accounts size, size:{}", size);
    }

}

