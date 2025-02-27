package crfa.app.service.processor.total;

import crfa.app.client.metadata.DappReleaseItem;
import crfa.app.domain.*;
import crfa.app.repository.PoolRepository;
import crfa.app.repository.total.DappsRepository;
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

import static crfa.app.domain.Purpose.SPEND;
import static crfa.app.domain.SnapshotType.*;
import static crfa.app.service.processor.total.ProcessorHelper.*;


// DappsFeedProcessor handles top level list-dapps case
@Singleton
@Slf4j
public class DappFeedProcessor implements FeedProcessor {

    @Inject
    private DappReleaseCacheHelper dappReleaseCacheHelper;

    @Inject
    private DappsRepository dappsRepository;

    @Inject
    private ScrollsOnChainDataService scrollsOnChainDataService;

    @Inject
    private PoolRepository poolRepository;

    @Override
    public void process(DappFeed dappFeed, InjestionMode injestionMode) {
        if (injestionMode == InjestionMode.WITHOUT_EPOCHS_ONLY_AGGREGATES) {
            return;
        }

        val dapps = new ArrayList<DApp>();

        val maxReleaseCache = dappReleaseCacheHelper.buildMaxReleaseVersionCache();

        dappFeed.getDappSearchResult().forEach(dappSearchItem -> {
            val dapp = new DApp();

            dapp.setId(dappSearchItem.getId());
            dapp.setName(dappSearchItem.getName());
            dapp.setLink(dappSearchItem.getUrl());
            dapp.setIcon(dappSearchItem.getIcon());
            dapp.setCategory(dappSearchItem.getCategory());
            dapp.setSubCategory(dappSearchItem.getSubCategory());
            dapp.setUpdateTime(new Date());
            dapp.setDAppType(DAppType.valueOf(dappSearchItem.getType()));
            dapp.setTwitter(dappSearchItem.getTwitter());

            var minTransactionsCount = 0L;

            var balance = 0L;
            var spendTransactionsCount = 0L;
            var spendVolume = 0L;
            var spendTrxFees = 0L;
            var spendTrxSizes = 0L;

            for (val dappReleaseItem : dappSearchItem.getReleases()) {
                val maxVersion = maxReleaseCache.getIfPresent(dapp.getId());

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

                    if (scriptItem.getPurpose() == SPEND) {
                        balance += loadBalance(dappFeed, hash);

                        spendTransactionsCount += loadSpendTransactionsCount(dappFeed, hash);
                        spendVolume += loadSpendVolume(dappFeed, hash);
                        spendTrxFees += loadSpendTrxFee(dappFeed, hash);
                        spendTrxSizes += loadSpendTrxSize(dappFeed, hash);
                    }
                    if (scriptItem.getPurpose() == Purpose.MINT) {
                        minTransactionsCount += loadMintTransactionsCount(dappFeed, hash);
                        if (scriptItem.getAssetId().isPresent()) {
                            balance += loadTokensBalance(dappFeed, scriptItem.getAssetId().orElseThrow());
                        }
                    }
                }
            }

            dapp.setMintTransactions(minTransactionsCount);

            dapp.setBalance(balance);
            dapp.setSpendTransactions(spendTransactionsCount);
            dapp.setSpendVolume(spendVolume);
            dapp.setSpendTrxFees(spendTrxFees);
            dapp.setSpendTrxSizes(spendTrxSizes);

            dapp.setSpendUniqueAccounts(scrollsOnChainDataService.getDappEpochEpochSnapshot(dapp.getId(), ALL));
            dapp.setSpendUniqueAccounts_lastEpoch(scrollsOnChainDataService.getDappEpochEpochSnapshot(dapp.getId(), ONE));
            dapp.setSpendUniqueAccounts_six_epochs_ago(scrollsOnChainDataService.getDappEpochEpochSnapshot(dapp.getId(), SIX));
            dapp.setSpendUniqueAccounts_eighteen_epochs_ago(scrollsOnChainDataService.getDappEpochEpochSnapshot(dapp.getId(), EIGHTEEN));

            dapp.setTransactions(minTransactionsCount + spendTransactionsCount);

            dapps.add(dapp);
        });

        log.info("Upserting dapps, count:{}...", dapps.size());
        dapps.forEach(dapp -> dappsRepository.upsertDApp(dapp));
        log.info("Upserted dapps.");

        dappsRepository.removeAllExcept(dapps);
    }

    private static boolean isLastVersion(DappReleaseItem dappReleaseItem, Float maxVersion) {
        return Optional.ofNullable(maxVersion)
                .map(v -> Float.compare(dappReleaseItem.getReleaseNumber(), v) == 0)
                .orElse(true);
    }

}
