package crfa.app.service.processor;

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

import static crfa.app.service.processor.ProcessorHelper.*;

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

                        newDappReleaseItem.setHash(scriptItem.getScriptHash());
                        newDappReleaseItem.setScriptType(ScriptType.SPEND);
                        newDappReleaseItem.setScriptInvocationsCount(loadInvocationsPerHash(dappFeed, scriptHash));

                        val contractAddress = scriptItem.getContractAddress();
                        newDappReleaseItem.setContractAddress(contractAddress);
                        newDappReleaseItem.setScriptsLocked(loadAddressBalance(dappFeed, contractAddress));
                        newDappReleaseItem.setTransactionsCount(loadTransactionsCount(dappFeed, contractAddress));
                    }
                    if (scriptItem.getPurpose() == Purpose.MINT) {
                        val mintPolicyID = scriptItem.getMintPolicyID();

                        newDappReleaseItem.setHash(scriptItem.getMintPolicyID());
                        newDappReleaseItem.setMintPolicyID(scriptItem.getMintPolicyID());
                        newDappReleaseItem.setScriptType(ScriptType.MINT);
                        newDappReleaseItem.setScriptInvocationsCount(loadInvocationsPerHash(dappFeed, mintPolicyID));

                        if (scriptItem.getAssetId().isPresent()) {
                            val assetId = scriptItem.getAssetId().get();
                            // TODO make it better code
                            newDappReleaseItem.setScriptsLocked(newDappReleaseItem.getScriptsLocked() != null ? (newDappReleaseItem.getScriptsLocked() + loadTokensBalance(dappFeed, assetId)) : loadTokensBalance(dappFeed, assetId));
                        }
                    }

                    dappScriptItems.add(newDappReleaseItem);
                }
            });
        });

        log.info("Upserting dapp script items..., itemsCount:{}", dappScriptItems.size());
        dappScriptItems.forEach(dappScriptItem -> {
            log.debug("Upserting, dapp item:{} - {}", dappScriptItem.getName(), dappScriptItem.getDappId());
            dappScriptsRepository.update(dappScriptItem);
        });
        log.info("Upserted dapp script items.");

        dappScriptsRepository.removeAllExcept(dappScriptItems);
    }

}
