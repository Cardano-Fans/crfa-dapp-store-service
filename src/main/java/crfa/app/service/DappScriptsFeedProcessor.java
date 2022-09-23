package crfa.app.service;

import crfa.app.domain.DappFeed;
import crfa.app.domain.DappScriptItem;
import crfa.app.domain.Purpose;
import crfa.app.domain.ScriptType;
import crfa.app.repository.DappScriptsRepository;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import lombok.extern.slf4j.Slf4j;
import lombok.val;

import java.util.ArrayList;
import java.util.Date;

@Singleton
@Slf4j
// DapppReleasesFeedProcessor handles low level - release items (scripts)
public class DappScriptsFeedProcessor implements FeedProcessor {

    @Inject
    private DappScriptsRepository dappScriptsRepository;

    @Override
    public void process(DappFeed dappFeed) {
        val dappScriptItems = new ArrayList<DappScriptItem>();

        dappFeed.getDappSearchResult().forEach(dappSearchItem -> { // looping over dapps
            dappSearchItem.getReleases().forEach(dappReleaseItem -> { // looping over dapp releases
                for (val scriptItem : dappReleaseItem.getScripts()) { // looping over dapp scripts om release level
                    val newDappReleaseItem = new DappScriptItem();
                    newDappReleaseItem.setName(scriptItem.getName());
                    newDappReleaseItem.setDappId(dappSearchItem.getId());
                    newDappReleaseItem.setReleaseKey(String.format("%s.%f", dappSearchItem.getId(), dappReleaseItem.getReleaseNumber()));
                    newDappReleaseItem.setVersion(scriptItem.getVersion());
                    newDappReleaseItem.setUpdateTime(new Date());

                    Long invocationsPerHash = null;
                    if (scriptItem.getPurpose() == Purpose.SPEND) {
                        val scriptHash = scriptItem.getScriptHash();

                        invocationsPerHash = dappFeed.getInvocationsCountPerHash().get(scriptHash);
                        if (invocationsPerHash == null) {
                            log.warn("Unable to find total invocations for scriptHash:{}, id:{}", scriptHash, scriptItem.getId());
                        }
                        newDappReleaseItem.setScriptType(ScriptType.SPEND);
                        newDappReleaseItem.setHash(scriptItem.getScriptHash());
                        newDappReleaseItem.setContractAddress(scriptItem.getContractAddress());
                    }
                    if (scriptItem.getPurpose() == Purpose.MINT) {
                        val mintPolicyID = scriptItem.getMintPolicyID();
                        invocationsPerHash = dappFeed.getInvocationsCountPerHash().get(mintPolicyID);

                        if (invocationsPerHash == null) {
                            log.warn("Unable to find total invocations for mintPolicyID:{}, id:{}", mintPolicyID, scriptItem.getId());
                        }

                        newDappReleaseItem.setScriptType(ScriptType.MINT);
                        newDappReleaseItem.setHash(scriptItem.getMintPolicyID());
                        newDappReleaseItem.setMintPolicyID(scriptItem.getMintPolicyID());
                        if (dappFeed.getTokenHoldersBalance() != null && scriptItem.getAssetId().isPresent()) {
                            val aseetId =  scriptItem.getAssetId().get();
                            val adaBalance = dappFeed.getTokenHoldersBalance().get(aseetId);
                            if (adaBalance != null) {
                                log.info("setting script balance for aseetId:{}, ada balance:{}", aseetId, adaBalance);
                                newDappReleaseItem.setScriptsLocked(adaBalance);
                            }
                        }
                    }

                    if (invocationsPerHash != null) {
                        newDappReleaseItem.setScriptInvocationsCount(invocationsPerHash);
                    } else {
                        newDappReleaseItem.setScriptInvocationsCount(0L);
                    }

                    val contractAddress = scriptItem.getContractAddress();
                    if (contractAddress != null && scriptItem.getPurpose() == Purpose.SPEND) {
                        val scriptsLocked = dappFeed.getScriptLockedPerContractAddress().get(contractAddress);
                        if (scriptsLocked != null) {
                            newDappReleaseItem.setScriptsLocked(scriptsLocked);
                        } else {
                            log.warn("Unable to find scriptsLocked for contractAddress:{}", contractAddress);
                        }

                        val transactionsCount = dappFeed.getTransactionCountsPerContractAddress().get(contractAddress);
                        if (transactionsCount != null) {
                            newDappReleaseItem.setTransactionsCount(transactionsCount);
                        }
                    }

                    dappScriptItems.add(newDappReleaseItem);
                }

                dappScriptItems.forEach(dappScriptItem -> {
                    log.debug("Upserting, dapp item:{} - {}", dappScriptItem.getName(), dappReleaseItem.getReleaseName());
                    dappScriptsRepository.update(dappScriptItem);
                });
            });
        });

        dappScriptsRepository.removeAllExcept(dappScriptItems);
    }

}
