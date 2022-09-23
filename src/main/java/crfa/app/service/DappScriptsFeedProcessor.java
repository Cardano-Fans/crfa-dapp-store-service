package crfa.app.service;

import crfa.app.client.metadata.ScriptItem;
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
                for (val scriptItem : dappReleaseItem.getScripts()) { // looping over dapp scripts on release level
                    val newDappReleaseItem = new DappScriptItem();
                    newDappReleaseItem.setName(scriptItem.getName());
                    newDappReleaseItem.setDappId(dappSearchItem.getId());
                    newDappReleaseItem.setReleaseKey(String.format("%s.%.1f", dappSearchItem.getId(), dappReleaseItem.getReleaseNumber()));
                    newDappReleaseItem.setVersion(scriptItem.getVersion());
                    newDappReleaseItem.setUpdateTime(new Date());

                    if (scriptItem.getPurpose() == Purpose.SPEND) {
                        val scriptHash = scriptItem.getScriptHash();

                        newDappReleaseItem.setScriptType(ScriptType.SPEND);
                        newDappReleaseItem.setScriptInvocationsCount(loadInvocationsPerHash(dappFeed, scriptItem, scriptHash));
                        newDappReleaseItem.setHash(scriptItem.getScriptHash());

                        val contractAddress = scriptItem.getContractAddress();
                        if (contractAddress == null) {
                            log.warn("contract addr for script type SPEND is null! scriptHash:{}", scriptHash);
                        } else {
                            newDappReleaseItem.setContractAddress(contractAddress);
                            newDappReleaseItem.setScriptsLocked(loadAddressBalance(dappFeed, contractAddress));
                            newDappReleaseItem.setTransactionsCount(loadTransactionsCount(dappFeed, contractAddress));
                        }
                    }
                    if (scriptItem.getPurpose() == Purpose.MINT) {
                        val mintPolicyID = scriptItem.getMintPolicyID();

                        newDappReleaseItem.setScriptType(ScriptType.MINT);
                        newDappReleaseItem.setScriptInvocationsCount(loadInvocationsCountPerHash(dappFeed, mintPolicyID));
                        newDappReleaseItem.setHash(scriptItem.getMintPolicyID());
                        newDappReleaseItem.setMintPolicyID(scriptItem.getMintPolicyID());

                        if (dappFeed.getTokenHoldersBalance() != null && scriptItem.getAssetId().isPresent()) {
                            newDappReleaseItem.setScriptsLocked(loadAdaBalance(dappFeed, scriptItem.getAssetId().get()));
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

    private static Long loadAdaBalance(DappFeed dappFeed, String assetId) {
        return dappFeed.getTokenHoldersBalance().computeIfAbsent(assetId, aId -> {
            log.warn("Unable to load balance for assetId:{}", assetId);
            return 0L;
        });
    }

    private static Long loadInvocationsPerHash(DappFeed dappFeed, ScriptItem scriptItem, String scriptHash) {
        return dappFeed.getInvocationsCountPerHash().computeIfAbsent(scriptHash, hash -> {
            log.warn("Unable to find total invocations for scriptHash:{}, id:{}", hash, scriptItem.getId());

            return 0L;
        });
    }

    private static Long loadInvocationsCountPerHash(DappFeed dappFeed, String mintPolicyID) {
        return dappFeed.getInvocationsCountPerHash().computeIfAbsent(mintPolicyID, hash -> {
            log.warn("Unable to find invocationsPerHash hash:{}", hash);
            return 0L;
        });
    }

    private static Long loadAddressBalance(DappFeed dappFeed, String contractAddress) {
        return dappFeed.getScriptLockedPerContractAddress().computeIfAbsent(contractAddress, addr -> {
            log.warn("Unable to find scriptsLocked for contractAddress:{}", addr);

            return 0L;
        });
    }

    private static Long loadTransactionsCount(DappFeed dappFeed, String contractAddress) {
        return dappFeed.getTransactionCountsPerContractAddress().computeIfAbsent(contractAddress, addr -> {
            log.warn("Unable to find transactionsCount for contractAddress:{}", addr);

            return 0L;
        });
    }

}
