package crfa.app.service.processor.epoch;

import com.google.common.cache.Cache;
import com.google.common.collect.HashMultiset;
import crfa.app.client.metadata.DappReleaseItem;
import crfa.app.client.metadata.DappSearchItem;
import crfa.app.domain.*;
import crfa.app.repository.epoch.DappsEpochRepository;
import crfa.app.service.DappService;
import crfa.app.service.processor.FeedProcessor;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import lombok.extern.slf4j.Slf4j;
import lombok.val;

import java.util.ArrayList;
import java.util.Date;
import java.util.Optional;

import static crfa.app.domain.EraName.ALONZO;
import static crfa.app.service.processor.epoch.ProcessorHelper.*;

@Singleton
@Slf4j

// DappsFeedProcessor handles top level list-dapps case
public class DappFeedEpochProcessor implements FeedProcessor {

    @Inject
    private DappService dappService;

    @Inject
    private DappsEpochRepository dappsEpochRepository;

    @Override
    public void process(DappFeed dappFeed, InjestionMode injestionMode) {
        if (injestionMode == InjestionMode.WITHOUT_EPOCHS_ONLY_AGGREGATES) {
            log.info("epoch level ingestion disabled.");
            return;
        }

        val currentEpochNo = dappService.currentEpoch();

        val dapps = new ArrayList<DAppEpoch>();

        val maxReleaseCache = dappService.buildMaxReleaseVersionCache();

        dappFeed.getDappSearchResult().forEach(dappSearchItem -> {
            dappSearchItem.getReleases().forEach(dappReleaseItem -> {
                val injestCurrentEpochOnly = injestionMode == InjestionMode.CURRENT_EPOCH_AND_AGGREGATES;

                if (injestCurrentEpochOnly) {
                    dapps.add(createDappEpoch(dappFeed, false, maxReleaseCache, dappSearchItem, dappReleaseItem, currentEpochNo));
                } else {
                    for (val epochNo : Eras.epochsBetween(ALONZO, currentEpochNo)) {
                        val isClosedEpoch = epochNo < currentEpochNo;
                        dapps.add(createDappEpoch(dappFeed, isClosedEpoch, maxReleaseCache, dappSearchItem, dappReleaseItem, epochNo));
                    }
                }
            });
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
                                          DappReleaseItem dappReleaseItem,
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

        var totalInflowsOutflows = 0L;
        var totalScriptInvocations = 0L;
        var totalTransactionsCount = 0L;
        var totalVolume = 0L;
        var totalUniqueAccounts = HashMultiset.<String>create();

        var lastVersionTotalInflowsOutflows = 0L;
        var lastVersionTotalScriptInvocations = 0L;
        var lastVersionTotalTransactionsCount = 0L;
        var lastVersionTotalVolume = 0L;
        var lastVersionTotalUniqueAccounts = HashMultiset.<String>create();

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

            if (scriptItem.getPurpose() == Purpose.SPEND) {
                val scriptHash = scriptItem.getScriptHash();
                val invocationsPerHash = loadInvocationsPerHash(dappFeed, scriptHash, epochNo);
                totalScriptInvocations += invocationsPerHash;
                if (isLastVersion) {
                    lastVersionTotalScriptInvocations = invocationsPerHash;
                }

                val contractAddress = scriptItem.getContractAddress();

                val adaBalance = loadAddressBalance(dappFeed, contractAddress, epochNo);
                totalInflowsOutflows += adaBalance;

                if (isLastVersion) {
                    lastVersionTotalInflowsOutflows += adaBalance;
                }

                val transactionsCount = loadTransactionsCount(dappFeed, contractAddress, epochNo);
                totalTransactionsCount += transactionsCount;
                if (isLastVersion) {
                    lastVersionTotalTransactionsCount += transactionsCount;
                }

                val volume = loadVolume(dappFeed, contractAddress, epochNo);
                totalVolume += volume;
                if (isLastVersion) {
                    lastVersionTotalVolume += volume;
                }

                val uniqueAccounts = loadUniqueAccounts(dappFeed, contractAddress, epochNo);
                totalUniqueAccounts.addAll(uniqueAccounts);
                if (isLastVersion) {
                    lastVersionTotalUniqueAccounts.addAll(uniqueAccounts);
                }
            }
            if (scriptItem.getPurpose() == Purpose.MINT) {
                val mintPolicyID = scriptItem.getMintPolicyID();
                val invocationsPerHash = loadInvocationsPerHash(dappFeed, mintPolicyID, epochNo);

                totalScriptInvocations += invocationsPerHash;
                if (isLastVersion) {
                    lastVersionTotalScriptInvocations = invocationsPerHash;
                }
                // wind riders case
                if (scriptItem.getAssetId().isPresent()) {
                    val assetId = scriptItem.getAssetId().get();
                    val tokenAdaBalance = loadTokensBalance(dappFeed, assetId, epochNo);
                    totalInflowsOutflows += tokenAdaBalance;
                    if (isLastVersion) {
                        lastVersionTotalInflowsOutflows += tokenAdaBalance;
                    }
                }
            }

            dapp.setScriptInvocationsCount(totalScriptInvocations);
            dapp.setInflowsOutflows(totalInflowsOutflows);
            dapp.setTransactionsCount(totalTransactionsCount);
            dapp.setVolume(totalVolume);
            dapp.setUniqueAccounts(totalUniqueAccounts.size());

            dapp.setLastVersionInflowsOutflows(lastVersionTotalInflowsOutflows);
            dapp.setLastVersionTransactionsCount(lastVersionTotalTransactionsCount);
            dapp.setLastVersionScriptInvocationsCount(lastVersionTotalScriptInvocations);
            dapp.setLastVersionVolume(lastVersionTotalVolume);
            dapp.setLastVersionUniqueAccounts(lastVersionTotalUniqueAccounts.size());
        }

        return dapp;
    }

    private static boolean isLastVersion(DappReleaseItem dappReleaseItem, Float maxVersion) {
        return Optional.ofNullable(maxVersion)
                .map(v -> Float.compare(dappReleaseItem.getReleaseNumber(), v) == 0)
                .orElse(true);
    }

}
