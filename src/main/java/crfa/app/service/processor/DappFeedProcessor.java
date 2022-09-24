package crfa.app.service.processor;

import crfa.app.client.metadata.DappReleaseItem;
import crfa.app.domain.DApp;
import crfa.app.domain.DAppType;
import crfa.app.domain.DappFeed;
import crfa.app.domain.Purpose;
import crfa.app.repository.DappsRepository;
import crfa.app.service.DappService;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import lombok.extern.slf4j.Slf4j;
import lombok.val;

import java.util.ArrayList;
import java.util.Date;
import java.util.Optional;

import static crfa.app.service.processor.ProcessorHelper.*;

@Singleton
@Slf4j

// DappsFeedProcessor handles top level list-dapps case
public class DappFeedProcessor implements FeedProcessor {

    @Inject
    private DappService dappService;

    @Inject
    private DappsRepository dappsRepository;

    @Override
    public void process(DappFeed dappFeed) {
        val dapps = new ArrayList<DApp>();

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

            val maxReleaseCache = dappService.buildMaxReleaseVersionCache();

            var totalScriptsLocked = 0L;
            var totalScriptInvocations = 0L;
            var totalTransactionsCount = 0L;

            var lastVersionTotalScriptsLocked = 0L;
            var lastVersionTotalScriptInvocations = 0L;
            var lastVersionTotalTransactionsCount = 0L;

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

                    if (scriptItem.getPurpose() == Purpose.SPEND) {
                        val invocationsPerHash = loadInvocationsPerHash(dappFeed, scriptItem.getScriptHash());

                        totalScriptInvocations += invocationsPerHash;
                        if (isLastVersion) {
                            lastVersionTotalScriptInvocations = invocationsPerHash;
                        }

                        val contractAddress = scriptItem.getContractAddress();
                        val adaBalance = loadAddressBalance(dappFeed, contractAddress);
                        totalScriptsLocked += adaBalance;
                        if (isLastVersion) {
                            lastVersionTotalScriptsLocked += adaBalance;
                        }

                        val transactionsCount = loadTransactionsCount(dappFeed, contractAddress);
                        totalTransactionsCount += transactionsCount;
                        if (isLastVersion) {
                            lastVersionTotalTransactionsCount += transactionsCount;
                        }
                    }
                    if (scriptItem.getPurpose() == Purpose.MINT) {
                        val invocationsPerHash = loadInvocationsPerHash(dappFeed, scriptItem.getMintPolicyID());

                        totalScriptInvocations += invocationsPerHash;
                        if (isLastVersion) {
                            lastVersionTotalScriptInvocations = invocationsPerHash;
                        }
                        // wind riders case
                        if (scriptItem.getAssetId().isPresent()) {
                            val tokenAdaBalance = loadTokensBalance(dappFeed, scriptItem.getAssetId().get());
                            totalScriptsLocked += tokenAdaBalance;
                            if (isLastVersion) {
                                lastVersionTotalScriptsLocked += tokenAdaBalance;
                            }
                        }
                    }
                }

                dapp.setScriptInvocationsCount(totalScriptInvocations);
                dapp.setScriptsLocked(totalScriptsLocked);
                dapp.setTransactionsCount(totalTransactionsCount);

                dapp.setLastVersionScriptsLocked(lastVersionTotalScriptsLocked);
                dapp.setLastVersionTransactionsCount(lastVersionTotalTransactionsCount);
                dapp.setLastVersionScriptInvocationsCount(lastVersionTotalScriptInvocations);

                dapps.add(dapp);
            }
        });

        log.info("Upserting dapps, count:{}...", dapps.size());

        dapps.forEach(dapp -> {
            dappsRepository.upsertDApp(dapp);
        });

        log.info("Upserted dapps.");

        dappsRepository.removeAllExcept(dapps);
    }

    private static boolean isLastVersion(DappReleaseItem dappReleaseItem, Float maxVersion) {
        return Optional.ofNullable(maxVersion)
                .map(v -> Float.compare(dappReleaseItem.getReleaseNumber(), v) == 0)
                .orElse(true);
    }

}
