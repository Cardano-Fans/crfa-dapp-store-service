package crfa.app.service.processor.pre;

import crfa.app.domain.DappFeed;
import crfa.app.domain.Eras;
import crfa.app.domain.InjestionMode;
import crfa.app.service.ScrollsOnChainDataService;
import crfa.app.service.processor.FeedPreProcessor;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import lombok.extern.slf4j.Slf4j;
import lombok.val;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

import static crfa.app.domain.Purpose.SPEND;
import static crfa.app.domain.SnapshotType.*;

@Singleton
@Slf4j
public class DappSnapshotsFeedPreProcessor implements FeedPreProcessor {

    @Inject
    private ScrollsOnChainDataService scrollsOnChainDataService;

    @Override
    public void process(DappFeed dappFeed, InjestionMode injestionMode) {
        log.info("dapp snapshots feed pre-processor...");

        val hashesPerDapp = new HashMap<String, Set<String>>();

        val currentEpochNo = scrollsOnChainDataService.currentEpoch().orElseThrow();

        val one = Eras.epochsBetween(ONE.startEpoch(currentEpochNo), currentEpochNo);
        val six = Eras.epochsBetween(SIX.startEpoch(currentEpochNo), currentEpochNo);
        val eighteen = Eras.epochsBetween(EIGHTEEN.startEpoch(currentEpochNo), currentEpochNo);
        val all = Eras.epochsBetween(ALL.startEpoch(currentEpochNo), currentEpochNo);

        for (val dsi : dappFeed.getDappSearchResult()) {
            hashesPerDapp.computeIfAbsent(dsi.getId(), k -> new HashSet<>());

            for (val r : dsi.getReleases()) {
                for (val s : r.getScripts()) {
                    if (s.getPurpose() == SPEND) {
                        hashesPerDapp.get(dsi.getId()).add(s.getUnifiedHash());
                    }
                }
            }
        }

        hashesPerDapp.forEach((dappId, wallets) -> {
            scrollsOnChainDataService.storeDappEpochSnapshot(dappId, wallets, one, ONE);
            scrollsOnChainDataService.storeDappEpochSnapshot(dappId, wallets, six, SIX);
            scrollsOnChainDataService.storeDappEpochSnapshot(dappId, wallets, eighteen, EIGHTEEN);
            scrollsOnChainDataService.storeDappEpochSnapshot(dappId, wallets, all, ALL);

            //log.info("Stored snapshot unique accounts per dAppId:{}", dappId);
        });
    }

}

