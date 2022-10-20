package crfa.app.service.processor.total;

import crfa.app.client.metadata.DappReleaseItem;
import crfa.app.domain.*;
import crfa.app.repository.total.DappsRepository;
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

import static crfa.app.domain.Purpose.SPEND;
import static crfa.app.service.processor.total.ProcessorHelper.*;


// DappsFeedProcessor handles top level list-dapps case
@Singleton
@Slf4j
public class DappFeedProcessor implements FeedProcessor {

    @Inject
    private DappService dappService;

    @Inject
    private DappsRepository dappsRepository;

    @Override
    public void process(DappFeed dappFeed, InjestionMode injestionMode, FeedProcessingContext context) {
        val dapps = new ArrayList<DApp>();

        val maxReleaseCache = dappService.buildMaxReleaseVersionCache();

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

            var totalScriptsLocked = 0L;
            var totalScriptInvocations = 0L;
            var totalVolume = 0L;
            var fees = 0L;

            var totalUniqueAccounts = new HashSet<String>();

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
                    totalScriptInvocations += loadInvocations(dappFeed, hash);

                    if (scriptItem.getPurpose() == SPEND) {
                        totalVolume += loadVolume(dappFeed, hash);
                        fees += loadFees(dappFeed, hash);
                        totalScriptsLocked += loadAdaBalance(dappFeed, hash);
                        totalUniqueAccounts.addAll(loadUniqueAccounts(dappFeed, hash));
                    }
                    if (scriptItem.getPurpose() == Purpose.MINT && scriptItem.getAssetId().isPresent()) {
                        totalScriptsLocked += loadTokensBalance(dappFeed, scriptItem.getAssetId().get());
                    }

                    context.getUniqueAccounts().addAll(totalUniqueAccounts);
                }
            }

            dapp.setScriptInvocationsCount(totalScriptInvocations);
            dapp.setScriptsLocked(totalScriptsLocked);
            dapp.setVolume(totalVolume);
            dapp.setFees(fees);
            dapp.setUniqueAccounts(totalUniqueAccounts.size());

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
