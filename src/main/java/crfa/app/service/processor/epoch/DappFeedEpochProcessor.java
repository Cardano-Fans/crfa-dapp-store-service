package crfa.app.service.processor.epoch;

import com.google.common.cache.Cache;
import crfa.app.client.metadata.DappReleaseItem;
import crfa.app.client.metadata.DappSearchItem;
import crfa.app.domain.*;
import crfa.app.repository.epoch.DappsEpochRepository;
import crfa.app.service.DappReleaseCacheHelper;
import crfa.app.service.ScrollsOnChainDataService;
import crfa.app.service.processor.FeedProcessor;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import lombok.extern.slf4j.Slf4j;
import lombok.val;

import java.util.ArrayList;
import java.util.Date;
import java.util.Optional;

import static crfa.app.domain.Purpose.MINT;
import static crfa.app.service.processor.epoch.ProcessorHelper.*;

@Singleton
@Slf4j

// DappsFeedProcessor handles top level list-dapps case
public class DappFeedEpochProcessor implements FeedProcessor {

    @Inject
    private DappReleaseCacheHelper dappReleaseCacheHelper;

    @Inject
    private DappsEpochRepository dappsEpochRepository;

    @Inject
    private ScrollsOnChainDataService scrollsOnChainDataService;

    @Override
    public boolean isEpochProcessor() {
        return true;
    }

    @Override
    public void process(DappFeed dappFeed, InjestionMode injestionMode) {
        if (injestionMode == InjestionMode.WITHOUT_EPOCHS_ONLY_AGGREGATES) {
            return;
        }

        val currentEpochNo = scrollsOnChainDataService.currentEpoch().orElseThrow();

        val dapps = new ArrayList<DAppEpoch>();

        val maxReleaseCache = dappReleaseCacheHelper.buildMaxReleaseVersionCache();

        dappFeed.getDappSearchResult().forEach(dappSearchItem -> {
            val injestCurrentEpochOnly = injestionMode == InjestionMode.CURRENT_EPOCH_AND_AGGREGATES;

            if (injestCurrentEpochOnly) {
                val dappEpoch = createDappEpoch(dappFeed, false, maxReleaseCache, dappSearchItem, currentEpochNo);

                dapps.add(dappEpoch);
                return;
            }

            for (val epochNo : Eras.epochsBetween(SnapshotType.ALL.startEpoch(currentEpochNo), currentEpochNo)) {
                val isClosedEpoch = epochNo < currentEpochNo;
                val dappEpoch = createDappEpoch(dappFeed, isClosedEpoch, maxReleaseCache, dappSearchItem, epochNo);

                dapps.add(dappEpoch);
            }
        });

        log.info("Upserting dapps, count:{}...", dapps.size());

        dapps.forEach(dapp -> {
            dappsEpochRepository.upsertDApp(dapp);
        });

        log.info("Upserted dapps.");

        dappsEpochRepository.removeAllExcept(dapps);
    }

    private DAppEpoch createDappEpoch(DappFeed dappFeed,
                                      boolean isClosedEpoch,
                                      Cache<String, Float> maxReleaseCache,
                                      DappSearchItem dappSearchItem,
                                      int epochNo) {
        val dapp = new DAppEpoch();

        val dappId = dappSearchItem.getId();

        dapp.setId(String.format("%s.%d", dappId, epochNo));
        dapp.setDappId(dappId);
        dapp.setEpochNo(epochNo);
        dapp.setName(dappSearchItem.getName());
        dapp.setLink(dappSearchItem.getUrl());
        dapp.setIcon(dappSearchItem.getIcon());
        dapp.setCategory(dappSearchItem.getCategory());
        dapp.setSubCategory(dappSearchItem.getSubCategory());
        dapp.setUpdateTime(new Date());
        dapp.setDAppType(DAppType.valueOf(dappSearchItem.getType()));
        dapp.setTwitter(dappSearchItem.getTwitter());
        dapp.setClosedEpoch(isClosedEpoch);

        var mintTransactionsCount = 0L;

        var inflowsOutflows = 0L;
        var spendTransactionsCount = 0L;
        var spendVolume = 0L;
        var spendTrxFees = 0L;
        var spendTrxSizes = 0L;

        val maxVersion = maxReleaseCache.getIfPresent(dappId);

        for (val dappReleaseItem : dappSearchItem.getReleases()) {
            boolean isLastVersion = isLastVersion(dappReleaseItem, maxVersion);

            for (val scriptItem : dappReleaseItem.getScripts()) {
                Optional.ofNullable(dappReleaseItem.getContract()).ifPresent(contract -> {
                    if (isLastVersion && contract.getOpenSource() != null && contract.getOpenSource()) {
                        dapp.setLastVersionOpenSourceLink(contract.getContractLink());
                    }
                });

                Optional.ofNullable(dappReleaseItem.getAudit()).ifPresent(audit -> {
                    if (isLastVersion) {
                        dapp.setLastVersionAuditLink(audit.getAuditLink());
                    }
                });

                val hash = scriptItem.getUnifiedHash();

                if (scriptItem.getPurpose() == Purpose.SPEND) {
                    spendTransactionsCount += loadSpendTransactionsCount(dappFeed, hash, epochNo);
                    spendVolume += loadSpendVolume(dappFeed, hash, epochNo);
                    inflowsOutflows += loadBalance(dappFeed, hash, epochNo);
                    spendTrxFees += loadSpendTrxFee(dappFeed, hash, epochNo);
                    spendTrxSizes += loadTrxSize(dappFeed, hash, epochNo);
                }

                if (scriptItem.getPurpose() == MINT) {
                    mintTransactionsCount += loadMintTransactionsCount(dappFeed, hash, epochNo);

                    // wing riders case
                    if (scriptItem.getAssetId().isPresent()) {
                        inflowsOutflows += loadTokensBalance(dappFeed, scriptItem.getAssetId().orElseThrow(), epochNo);
                    }
                }
            }
            dapp.setMintTransactions(mintTransactionsCount);

            dapp.setInflowsOutflows(inflowsOutflows);
            dapp.setSpendTransactions(spendTransactionsCount);
            dapp.setSpendVolume(spendVolume);
            dapp.setSpendTrxFees(spendTrxFees);
            dapp.setSpendTrxSizes(spendTrxSizes);

            dapp.setTransactions(mintTransactionsCount + spendTransactionsCount);

            dapp.setSpendUniqueAccounts(scrollsOnChainDataService.getDappEpochSnapshot(dappId, epochNo));
        }

        return dapp;
    }

    private static boolean isLastVersion(DappReleaseItem dappReleaseItem, Float maxVersion) {
        return Optional.ofNullable(maxVersion)
                .map(v -> Float.compare(dappReleaseItem.getReleaseNumber(), v) == 0)
                .orElse(true);
    }

}
