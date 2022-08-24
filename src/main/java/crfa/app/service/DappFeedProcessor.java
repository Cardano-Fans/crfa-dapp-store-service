package crfa.app.service;

import crfa.app.client.metadata.ScriptItem;
import crfa.app.domain.DApp;
import crfa.app.domain.DAppType;
import crfa.app.domain.DappFeed;
import crfa.app.domain.Purpose;
import crfa.app.repository.DappsRepository;
import io.micronaut.context.annotation.Value;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import lombok.extern.slf4j.Slf4j;

import java.util.Date;
import java.util.Optional;

@Singleton
@Slf4j
public class DappFeedProcessor implements FeedProcessor {

    @Value("${dryRunMode:true}")
    private boolean dryRunMode;

    @Inject
    private DappService dappService;

    @Inject
    private DappsRepository dappsRepository;

    @Override
    public void process(DappFeed dappFeed) {
        dappFeed.getDappSearchResult().forEach(dappSearchItem -> {
            var dapp = new DApp();

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
            var totalInvocations = 0L;
            var totalTransactionsCount = 0L;

            var maxReleaseCache = dappService.buildMaxReleaseVersionCache();

            for (var dappReleaseItem : dappSearchItem.getReleases()) {
                for (ScriptItem scriptItem : dappReleaseItem.getScripts()) {
                    var contractAddress = scriptItem.getContractAddress();

                    var maxVersion = maxReleaseCache.getIfPresent(dapp.getId());
                    boolean isLastVersion = Float.compare(dappReleaseItem.getReleaseNumber(), maxVersion) == 0;

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

                    // deprecated to remove - backwards compatibility for now
//                    if (dapp.getLastVersionAudited() == null) {
//                        Optional.ofNullable(scriptItem.getAudit()).ifPresent(audit -> {
//                            if (isLastVersion) {
//                                dapp.setLastVersionAudited(audit.getAuditLink() != null);
//                            }
//                        });
//                    }

                    // deprecated to remove - backwards compatibility for now
//                    if (dapp.getLastVersionOpenSourced() == null) {
//                        Optional.ofNullable(scriptItem.getContract()).ifPresent(contract -> {
//                            if (isLastVersion) {
//                                dapp.setLastVersionOpenSourced(contract.getOpenSource());
//                            }
//                        });
//                    }

                    Long invocationsPerHash = null;
                    if (scriptItem.getPurpose() == Purpose.SPEND) {
                        var scriptHash = scriptItem.getScriptHash();

                        invocationsPerHash = dappFeed.getInvocationsCountPerHash().get(scriptHash);
                        if (invocationsPerHash == null) {
                            log.warn("Unable to find total invocations for scriptHash:{}, id:{}", scriptHash, scriptItem.getId());
                        }
                    }
                    if (scriptItem.getPurpose() == Purpose.MINT) {
                        var mintPolicyID = scriptItem.getMintPolicyID();
                        invocationsPerHash = dappFeed.getInvocationsCountPerHash().get(mintPolicyID);

                        if (invocationsPerHash == null) {
                            log.warn("Unable to find total invocations for mintPolicyID:{}, id:{}", mintPolicyID, scriptItem.getId());
                        }
                    }

                    if (invocationsPerHash != null) {
                        totalInvocations += invocationsPerHash;
                    }
                    if (contractAddress != null && scriptItem.getPurpose() == Purpose.SPEND) {
                        var scriptsLocked = dappFeed.getScriptLockedPerContractAddress().get(contractAddress);
                        if (scriptsLocked != null) {
                            totalScriptsLocked += scriptsLocked;
                        } else {
                            log.warn("Unable to find scriptsLocked for contractAddress:{}", contractAddress);
                        }

                        var trxCount = dappFeed.getTransactionCountsPerContractAddress().get(contractAddress);
                        if (trxCount != null) {
                            totalTransactionsCount += trxCount;
                        }
                    }

                    if (dappFeed.getTokenHoldersBalance() != null && scriptItem.getPurpose() == Purpose.MINT && scriptItem.getAssetNameAsHex().isPresent()) {
                        final var assetNameHex = scriptItem.getAssetNameAsHex().get();
                        var adaBalance = dappFeed.getTokenHoldersBalance().get(assetNameHex);
                        if (adaBalance != null) {
                            log.info("Setting ada balance:{}, for assetNameHex:{}", adaBalance, assetNameHex);
                            totalScriptsLocked += adaBalance;
                        }
                    }
                }

                dapp.setScriptInvocationsCount(totalInvocations);
                dapp.setScriptsLocked(totalScriptsLocked);
                dapp.setTransactionsCount(totalTransactionsCount);

                if (!dryRunMode) {
                    log.debug("Upserting dapp, dappname:{}", dapp.getName());

                    dappsRepository.upsertDApp(dapp);
                }
            }
        });
    }

}
