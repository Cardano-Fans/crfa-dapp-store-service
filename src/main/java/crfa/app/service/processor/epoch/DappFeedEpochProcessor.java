package crfa.app.service.processor.epoch;

import com.google.common.cache.Cache;
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
import java.util.HashSet;
import java.util.Optional;

import static crfa.app.domain.EraName.ALONZO;
import static crfa.app.domain.Purpose.MINT;
import static crfa.app.service.processor.epoch.ProcessorHelper.*;
import static crfa.app.utils.MoreMath.safeDivision;

@Singleton
@Slf4j

// DappsFeedProcessor handles top level list-dapps case
public class DappFeedEpochProcessor implements FeedProcessor {

    @Inject
    private DappService dappService;

    @Inject
    private DappsEpochRepository dappsEpochRepository;

    @Override
    public boolean isEpochProcessor() {
        return true;
    }

    @Override
    public void process(DappFeed dappFeed, InjestionMode injestionMode, FeedProcessingContext context) {
        val currentEpochNo = dappService.currentEpoch();

        val dapps = new ArrayList<DAppEpoch>();

        val maxReleaseCache = dappService.buildMaxReleaseVersionCache();

        dappFeed.getDappSearchResult().forEach(dappSearchItem -> {
            val injestCurrentEpochOnly = injestionMode == InjestionMode.CURRENT_EPOCH_AND_AGGREGATES;

            if (injestCurrentEpochOnly) {
                val dappEpoch = createDappEpoch(dappFeed, false, maxReleaseCache, dappSearchItem, currentEpochNo, context);

                dapps.add(dappEpoch);
                return;
            }

            for (val epochNo : Eras.epochsBetween(ALONZO, currentEpochNo)) {
                val isClosedEpoch = epochNo < currentEpochNo;
                val dappEpoch = createDappEpoch(dappFeed, isClosedEpoch, maxReleaseCache, dappSearchItem, epochNo, context);

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

    private static DAppEpoch createDappEpoch(DappFeed dappFeed,
                                             boolean isClosedEpoch,
                                             Cache<String, Float> maxReleaseCache,
                                             DappSearchItem dappSearchItem,
                                             int epochNo,
                                             FeedProcessingContext context) {
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
        var totalVolume = 0L;
        var totalFees = 0L;
        var totalTrxSizes = 0L;
        var totalUniqueAccounts = new HashSet<String>();

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

                totalScriptInvocations += loadInvocations(dappFeed, hash, epochNo);

                if (scriptItem.getPurpose() == Purpose.SPEND) {
                    totalVolume += loadVolume(dappFeed, hash, epochNo);
                    totalFees += loadFee(dappFeed, hash, epochNo);
                    totalInflowsOutflows += loadAdaBalance(dappFeed, hash, epochNo);
                    totalTrxSizes += loadTrxSize(dappFeed, hash, epochNo);

                    totalUniqueAccounts.addAll(loadUniqueAccounts(dappFeed, hash, epochNo));
                }

                // wing riders case
                if (scriptItem.getPurpose() == MINT && scriptItem.getAssetId().isPresent()) {
                    totalInflowsOutflows += loadTokensBalance(dappFeed, scriptItem.getAssetId().get(), epochNo);
                }
            }

            dapp.setScriptInvocationsCount(totalScriptInvocations);
            dapp.setInflowsOutflows(totalInflowsOutflows);
            dapp.setVolume(totalVolume);
            dapp.setFees(totalFees);
            dapp.setTrxSizes(totalTrxSizes);

            dapp.setUniqueAccounts(totalUniqueAccounts.size());

            val accounts = context.getUniqueAccountsEpoch().getOrDefault(epochNo, new HashSet<>());
            accounts.addAll(totalUniqueAccounts);
            context.getUniqueAccountsEpoch().put(epochNo, accounts);
        }

        return dapp;
    }

    private static boolean isLastVersion(DappReleaseItem dappReleaseItem, Float maxVersion) {
        return Optional.ofNullable(maxVersion)
                .map(v -> Float.compare(dappReleaseItem.getReleaseNumber(), v) == 0)
                .orElse(true);
    }

}
