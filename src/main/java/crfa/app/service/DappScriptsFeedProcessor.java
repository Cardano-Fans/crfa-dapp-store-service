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

import static crfa.app.service.ProcessorHelper.*;

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
                        newDappReleaseItem.setScriptInvocationsCount(loadInvocationsPerHash(dappFeed, scriptHash));
                        newDappReleaseItem.setHash(scriptItem.getScriptHash());

                        val contractAddress = scriptItem.getContractAddress();
                        newDappReleaseItem.setContractAddress(contractAddress);
                        newDappReleaseItem.setScriptsLocked(loadAddressBalance(dappFeed, contractAddress));
                        newDappReleaseItem.setTransactionsCount(loadTransactionsCount(dappFeed, contractAddress));
                    }
                    if (scriptItem.getPurpose() == Purpose.MINT) {
                        val mintPolicyID = scriptItem.getMintPolicyID();

                        newDappReleaseItem.setScriptType(ScriptType.MINT);
                        newDappReleaseItem.setScriptInvocationsCount(loadInvocationsCountPerHash(dappFeed, mintPolicyID));
                        newDappReleaseItem.setHash(scriptItem.getMintPolicyID());
                        newDappReleaseItem.setMintPolicyID(scriptItem.getMintPolicyID());

                        if (scriptItem.getAssetId().isPresent()) {
                            val assetId = scriptItem.getAssetId().get();
                            newDappReleaseItem.setScriptsLocked(loadTokensBalance(dappFeed, assetId));
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
