package crfa.app.service;

import crfa.app.client.metadata.DappReleaseItem;
import crfa.app.client.metadata.ScriptItem;
import crfa.app.domain.DApp;
import crfa.app.domain.DAppType;
import crfa.app.domain.DappFeed;
import crfa.app.domain.Purpose;
import crfa.app.repository.DappsRepository;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import lombok.extern.slf4j.Slf4j;
import lombok.val;

import java.util.ArrayList;
import java.util.Date;
import java.util.Optional;

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

            var totalScriptsLocked = 0L;
            var totalScriptInvocations = 0L;
            var totalTransactionsCount = 0L;

            var lastVersionTotalScriptsLocked = 0L;
            var lastVersionTotalScriptInvocations = 0L;
            var lastVersionTotalTransactionsCount = 0L;

            val maxReleaseCache = dappService.buildMaxReleaseVersionCache();

            for (val dappReleaseItem : dappSearchItem.getReleases()) {
                val maxVersion = maxReleaseCache.getIfPresent(dapp.getId());

                boolean isLastVersion = isLastVersion(dappReleaseItem, maxVersion);

                for (ScriptItem scriptItem : dappReleaseItem.getScripts()) {
                    val contractAddress = scriptItem.getContractAddress();

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

                    Long invocationsPerHash = null;
                    if (scriptItem.getPurpose() == Purpose.SPEND) {
                        val scriptHash = scriptItem.getScriptHash();

                        invocationsPerHash = dappFeed.getInvocationsCountPerHash().get(scriptHash);
                        if (invocationsPerHash == null) {
                            log.warn("Unable to find total invocations for scriptHash:{}, id:{}", scriptHash, scriptItem.getId());
                        }
                    }
                    if (scriptItem.getPurpose() == Purpose.MINT) {
                        val mintPolicyID = scriptItem.getMintPolicyID();
                        invocationsPerHash = dappFeed.getInvocationsCountPerHash().get(mintPolicyID);

                        if (invocationsPerHash == null) {
                            log.warn("Unable to find total invocations for mintPolicyID:{}, id:{}", mintPolicyID, scriptItem.getId());
                        }
                    }

                    if (invocationsPerHash != null) {
                        if (isLastVersion) {
                            lastVersionTotalScriptInvocations += invocationsPerHash;
                        }
                        totalScriptInvocations += invocationsPerHash;
                    }
                    if (contractAddress != null && scriptItem.getPurpose() == Purpose.SPEND) {
                        val scriptsLocked = dappFeed.getScriptLockedPerContractAddress().get(contractAddress);
                        if (scriptsLocked != null) {
                            if (isLastVersion) {
                                lastVersionTotalScriptsLocked += scriptsLocked;
                            }
                            totalScriptsLocked += scriptsLocked;
                        } else {
                            log.warn("Unable to find scriptsLocked for contractAddress:{}", contractAddress);
                        }

                        val trxCount = dappFeed.getTransactionCountsPerContractAddress().get(contractAddress);
                        if (trxCount != null) {
                            if (isLastVersion) {
                                lastVersionTotalTransactionsCount += trxCount;
                            }
                            totalTransactionsCount += trxCount;
                        }
                    }

                    if (dappFeed.getTokenHoldersBalance() != null && scriptItem.getPurpose() == Purpose.MINT && scriptItem.getAssetId().isPresent()) {
                        val assetNameHex = scriptItem.getAssetId().get();
                        val adaBalance = dappFeed.getTokenHoldersBalance().get(assetNameHex);
                        if (adaBalance != null) {
                            log.info("Setting ada balance:{}, for assetNameHex:{}", adaBalance, assetNameHex);
                            if (isLastVersion) {
                                lastVersionTotalScriptsLocked += adaBalance;
                            }
                            totalScriptsLocked += adaBalance;
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

                log.debug("Upserting dapp, dappname:{}", dapp.getName());

                dappsRepository.upsertDApp(dapp);
            }
        });

        dappsRepository.removeAllExcept(dapps);
    }

    private static boolean isLastVersion(DappReleaseItem dappReleaseItem, Float maxVersion) {
        return Optional.ofNullable(maxVersion)
                .map(v -> Float.compare(dappReleaseItem.getReleaseNumber(), v) == 0)
                .orElse(true);
    }

}
