package crfa.app.service.processor.pre;

import crfa.app.domain.DAppRelease;
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
public class DappReleasesSnapshotsFeedPreProcessor implements FeedPreProcessor {

    @Inject
    private ScrollsOnChainDataService scrollsOnChainDataService;

    @Override
    public void process(DappFeed dappFeed, InjestionMode injestionMode) {
        log.info("dapp releases snapshots feed pre-processor...");

        val hashesPerDappReleaseId = new HashMap<String, Set<String>>();

        val currentEpochNo = scrollsOnChainDataService.currentEpoch().orElseThrow();

        val one = Eras.epochsBetween(ONE.startEpoch(currentEpochNo), currentEpochNo);
        val six = Eras.epochsBetween(SIX.startEpoch(currentEpochNo), currentEpochNo);
        val eighteen = Eras.epochsBetween(EIGHTEEN.startEpoch(currentEpochNo), currentEpochNo);
        val all = Eras.epochsBetween(ALL.startEpoch(currentEpochNo), currentEpochNo);

        for (val dsi : dappFeed.getDappSearchResult()) {
            for (val r : dsi.getReleases()) {
                val id = DAppRelease.createId(dsi, r);

                hashesPerDappReleaseId.computeIfAbsent(id, k -> new HashSet<>());

                for (val s : r.getScripts()) {
                    if (s.getPurpose() == SPEND) {
                        hashesPerDappReleaseId.get(id).add(s.getUnifiedHash());
                    }
                }
            }
        }

        hashesPerDappReleaseId.forEach((dappReleaseId, wallets) -> {
            scrollsOnChainDataService.storeDappReleaseEpochSnapshot(dappReleaseId, wallets, one, ONE);
            scrollsOnChainDataService.storeDappReleaseEpochSnapshot(dappReleaseId, wallets, six, SIX);
            scrollsOnChainDataService.storeDappReleaseEpochSnapshot(dappReleaseId, wallets, eighteen, EIGHTEEN);
            scrollsOnChainDataService.storeDappReleaseEpochSnapshot(dappReleaseId, wallets, all, ALL);
        });
    }

}

