package crfa.app.service.processor.pre;

import crfa.app.domain.DappFeed;
import crfa.app.domain.Eras;
import crfa.app.domain.InjestionMode;
import crfa.app.domain.SnapshotType;
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

@Singleton
@Slf4j
public class DappEpochSnapshotsFeedPreProcessor implements FeedPreProcessor {

    @Inject
    private ScrollsOnChainDataService scrollsOnChainDataService;

    @Override
    public void process(DappFeed dappFeed, InjestionMode injestionMode) {
        log.info("dapp snapshots feed pre-processor...");

        val currentEpochNo = scrollsOnChainDataService.currentEpoch().orElseThrow();

        val injestCurrentEpochOnly = injestionMode == InjestionMode.CURRENT_EPOCH_AND_AGGREGATES;

        if (injestCurrentEpochOnly) {
            val hashesPerDapp = new HashMap<String, Set<String>>();
            for (val dsi : dappFeed.getDappSearchResult()) {
                hashesPerDapp.computeIfAbsent(dsi.getId(), k -> new HashSet<>());

                for (val r : dsi.getReleases()) {
                    for (val s : r.getScripts()) {
                        if (s.getPurpose() == SPEND) {
                            val hash = s.getUnifiedHash();
                            hashesPerDapp.get(dsi.getId()).add(hash);
                        }
                    }
                }
            }

            hashesPerDapp.forEach((dappId, wallets) -> {
                scrollsOnChainDataService.storeDappEpochSnapshot(dappId, wallets, currentEpochNo);
            });

            return;
        }

        for (val epochNo : Eras.epochsBetween(SnapshotType.ALL.startEpoch(currentEpochNo), currentEpochNo)) {
            val hashesPerDapp = new HashMap<String, Set<String>>();

            for (val dsi : dappFeed.getDappSearchResult()) {
                hashesPerDapp.computeIfAbsent(dsi.getId(), k -> new HashSet<>());

                for (val r : dsi.getReleases()) {
                    for (val s : r.getScripts()) {
                        if (s.getPurpose() == SPEND) {
                            val hash = s.getUnifiedHash();
                            hashesPerDapp.get(dsi.getId()).add(hash);
                        }
                    }
                }
            }

            hashesPerDapp.forEach((dappId, wallets) -> {
                scrollsOnChainDataService.storeDappEpochSnapshot(dappId, wallets, epochNo);
            });
        }
    }

}

