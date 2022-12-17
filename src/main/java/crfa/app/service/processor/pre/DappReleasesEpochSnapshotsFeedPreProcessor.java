package crfa.app.service.processor.pre;

import crfa.app.domain.*;
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
public class DappReleasesEpochSnapshotsFeedPreProcessor implements FeedPreProcessor {

    @Inject
    private ScrollsOnChainDataService scrollsOnChainDataService;

    @Override
    public void process(DappFeed dappFeed, InjestionMode injestionMode) {
        log.info("dapp releases epoch snapshots feed pre-processor...");

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
                scrollsOnChainDataService.storeDappReleaseEpochSnapshot(dappId, wallets, currentEpochNo);
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
                scrollsOnChainDataService.storeDappReleaseEpochSnapshot(dappId, wallets, epochNo);
            });
        }
    }

}

