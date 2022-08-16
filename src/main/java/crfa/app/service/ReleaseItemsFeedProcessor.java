package crfa.app.service;

import crfa.app.client.metadata.ScriptItem;
import crfa.app.domain.DAppReleaseItem;
import crfa.app.domain.DappFeed;
import crfa.app.domain.Purpose;
import crfa.app.domain.ScriptType;
import crfa.app.repository.DappReleaseItemRepository;
import io.micronaut.context.annotation.Value;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.Date;

@Singleton
@Slf4j
public class ReleaseItemsFeedProcessor implements FeedProcessor {

    @Value("${dryRunMode:true}")
    private boolean dryRunMode;

    @Inject
    private DappReleaseItemRepository dappReleaseItemRepository;

    @Override
    public void process(DappFeed dappFeed) {
        var items = new ArrayList<DAppReleaseItem>();

        dappFeed.getDappSearchResult().forEach(dappSearchItem -> {

            dappSearchItem.getReleases().forEach(dappReleaseItem -> {
                for (ScriptItem scriptItem : dappReleaseItem.getScripts()) {
                    var newDappReleaseItem = new DAppReleaseItem();
                    newDappReleaseItem.setName(scriptItem.getName());
                    newDappReleaseItem.setDappId(dappSearchItem.getId());
                    newDappReleaseItem.setReleaseKey(String.format("%s.%f", dappSearchItem.getId(), dappReleaseItem.getReleaseNumber()));
                    newDappReleaseItem.setVersion(scriptItem.getVersion());
                    newDappReleaseItem.setUpdateTime(new Date());

                    Long invocationsPerHash = null;
                    if (scriptItem.getPurpose() == Purpose.SPEND) {
                        var scriptHash = scriptItem.getScriptHash();

                        invocationsPerHash = dappFeed.getInvocationsCountPerHash().get(scriptHash);
                        if (invocationsPerHash == null) {
                            log.warn("Unable to find total invocations for scriptHash:{}, id:{}", scriptHash, scriptItem.getId());
                        }
                        newDappReleaseItem.setScriptType(ScriptType.SPEND);
                        newDappReleaseItem.setHash(scriptItem.getScriptHash());
                        newDappReleaseItem.setContractAddress(scriptItem.getContractAddress());
                    }
                    if (scriptItem.getPurpose() == Purpose.MINT) {
                        var mintPolicyID = scriptItem.getMintPolicyID();
                        invocationsPerHash = dappFeed.getInvocationsCountPerHash().get(mintPolicyID);

                        if (invocationsPerHash == null) {
                            log.warn("Unable to find total invocations for mintPolicyID:{}, id:{}", mintPolicyID, scriptItem.getId());
                        }

                        newDappReleaseItem.setScriptType(ScriptType.MINT);
                        newDappReleaseItem.setHash(scriptItem.getMintPolicyID());
                        newDappReleaseItem.setMintPolicyID(scriptItem.getMintPolicyID());
                        if (dappFeed.getTokenHoldersBalance() != null && scriptItem.getAssetNameAsHex().isPresent()) {
                            var assetMameHex =  scriptItem.getAssetNameAsHex().get();
                            var adaBalance = dappFeed.getTokenHoldersBalance().get(assetMameHex);
                            if (adaBalance != null) {
                                log.info("setting script balance for assetMameHex:{}, ada balance:{}", assetMameHex, adaBalance);
                                newDappReleaseItem.setScriptsLocked(adaBalance);
                            }
                        }
                    }

                    if (invocationsPerHash != null) {
                        newDappReleaseItem.setScriptInvocationsCount(invocationsPerHash);
                    } else {
                        newDappReleaseItem.setScriptInvocationsCount(0L);
                    }
                    var contractAddress = scriptItem.getContractAddress();
                    if (contractAddress != null && scriptItem.getPurpose() == Purpose.SPEND) {
                        var scriptsLocked = dappFeed.getScriptLockedPerContractAddress().get(contractAddress);
                        if (scriptsLocked != null) {
                            newDappReleaseItem.setScriptsLocked(scriptsLocked);
                        } else {
                            log.warn("Unable to find scriptsLocked for contractAddress:{}", contractAddress);
                        }

                        var trxCount = dappFeed.getTransactionCountsPerContractAddress().get(contractAddress);
                        if (trxCount != null) {
                            newDappReleaseItem.setTransactionsCount(trxCount);
                        }
                    }

                    items.add(newDappReleaseItem);
                }

                if (!dryRunMode) {
                    items.forEach(dAppReleaseItem -> {
                        log.debug("Upserting, dapp item:{} - {}", dAppReleaseItem.getName(), dappReleaseItem.getReleaseName());
                        dappReleaseItemRepository.updatedAppReleaseItem(dAppReleaseItem);
                    });
                }
            });
        });
    }

}
