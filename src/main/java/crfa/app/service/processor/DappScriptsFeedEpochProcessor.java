package crfa.app.service.processor;

import crfa.app.client.metadata.DappReleaseItem;
import crfa.app.client.metadata.DappSearchItem;
import crfa.app.client.metadata.ScriptItem;
import crfa.app.domain.*;
import crfa.app.repository.DappScriptsEpochRepository;
import crfa.app.service.ScrollsOnChainDataService;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import lombok.extern.slf4j.Slf4j;
import lombok.val;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;

import static crfa.app.domain.EraName.ALONZO;
import static crfa.app.service.processor.ProcessorHelper.*;
import static java.lang.String.format;

@Singleton
@Slf4j
// this handles low level - release items (scripts)
public class DappScriptsFeedEpochProcessor implements FeedProcessor {

    @Inject
    private DappScriptsEpochRepository dappScriptsRepository;

    @Inject
    private ScrollsOnChainDataService scrollsOnChainDataService;

    @Override
    public void process(DappFeed dappFeed, InjestionMode injestionMode) {
        if (injestionMode == InjestionMode.WITHOUT_EPOCHS_ONLY_AGGREGATES) {
            log.info("epoch level ingestion disabled.");
            return;
        }

        val maybeCurrentEpochNo = scrollsOnChainDataService.currentEpoch();

        if (maybeCurrentEpochNo.isEmpty()) {
            log.warn("Unable to proceed, epoch level data won't be ingested.");
            return;
        }

        val currentEpochNo = maybeCurrentEpochNo.get();

        val dappScriptItems = new ArrayList<DappScriptItemEpoch>();

        dappFeed.getDappSearchResult().forEach(dappSearchItem -> { // looping over dapps
            dappSearchItem.getReleases().forEach(dappReleaseItem -> { // looping over dapp releases
                for (val scriptItem : dappReleaseItem.getScripts()) { // looping over dapp scripts on release level
                    val injestCurrentEpochOnly = injestionMode == InjestionMode.CURRENT_EPOCH_AND_AGGREGATES;

                    if (injestCurrentEpochOnly) {
                            dappScriptItems.add(createDappItemEpoch(dappFeed, currentEpochNo, dappSearchItem, dappReleaseItem, scriptItem, currentEpochNo));
                        } else {
                            for (val epochNo : Eras.epochsBetween(ALONZO, currentEpochNo)) {
                                dappScriptItems.add(createDappItemEpoch(dappFeed, currentEpochNo, dappSearchItem, dappReleaseItem, scriptItem, epochNo));
                            }
                        }
                }
            });
        });

        log.info("Upserting dapp script item epochs..., itemsCount:{}", dappScriptItems.size());

        dappScriptItems.forEach(dappScriptItemEpoch -> {
            log.debug("Upserting, dapp item:{} - id:{}, epochNo:{}", dappScriptItemEpoch.getName(), dappScriptItemEpoch.getId(), dappScriptItemEpoch.getEpochNo());
            dappScriptsRepository.update(dappScriptItemEpoch);
        });

        log.info("Upserted dapp script item epochs.");

        dappScriptsRepository.removeAllExcept(dappScriptItems);
    }

    private static DappScriptItemEpoch createDappItemEpoch(DappFeed dappFeed,
                                                           Integer currentEpochNo,
                                                           DappSearchItem dappSearchItem,
                                                           DappReleaseItem dappReleaseItem,
                                                           ScriptItem scriptItem,
                                                           Integer epochNo) {
        val dappScriptItem = new DappScriptItemEpoch();

        dappScriptItem.setName(scriptItem.getName());
        dappScriptItem.setDappId(dappSearchItem.getId());
        dappScriptItem.setReleaseKey(format("%s.%.1f", dappSearchItem.getId(), dappReleaseItem.getReleaseNumber()));
        dappScriptItem.setVersion(scriptItem.getVersion());
        dappScriptItem.setUpdateTime(new Date());
        dappScriptItem.setEpochNo(epochNo);
        dappScriptItem.setClosedEpoch(epochNo < currentEpochNo);

        if (scriptItem.getPurpose() == Purpose.SPEND) {
            val scriptHash = scriptItem.getScriptHash();

            dappScriptItem.setId(generateKey(epochNo, scriptHash));
            dappScriptItem.setHash(scriptHash);
            dappScriptItem.setScriptType(ScriptType.SPEND);
            dappScriptItem.setScriptInvocationsCount(loadInvocationsPerHash(dappFeed, scriptHash, epochNo));

            val contractAddress = scriptItem.getContractAddress();
            dappScriptItem.setContractAddress(contractAddress);
            dappScriptItem.setVolume(loadVolume(dappFeed, contractAddress, epochNo));
            dappScriptItem.setInflowsOutflows(loadAddressBalance(dappFeed, contractAddress, epochNo));
            dappScriptItem.setTransactionsCount(loadTransactionsCount(dappFeed, contractAddress, epochNo));
            dappScriptItem.setUniqueAccounts(loadUniqueAccounts(dappFeed, contractAddress, epochNo).size());
        }
        if (scriptItem.getPurpose() == Purpose.MINT) {
            val mintPolicyID = scriptItem.getMintPolicyID();

            dappScriptItem.setId(generateKey(epochNo, mintPolicyID));
            dappScriptItem.setHash(mintPolicyID);
            dappScriptItem.setScriptType(ScriptType.MINT);
            dappScriptItem.setScriptInvocationsCount(loadInvocationsPerHash(dappFeed, mintPolicyID, epochNo));
            dappScriptItem.setMintPolicyID(scriptItem.getMintPolicyID());

            if (scriptItem.getAssetId().isPresent()) {
                val assetId = scriptItem.getAssetId().get();
                // in case of purpouse = MINT there is no way we could have any script balance to add, so we only take tokens balance (ADA)
                dappScriptItem.setInflowsOutflows(loadTokensBalance(dappFeed, assetId, epochNo));

                val holders = dappFeed.getTokenHoldersAddresses().get(assetId);

                val allUniqueAccounts = new HashSet<>();

                holders.forEach(holder -> allUniqueAccounts.addAll(holders));

                dappScriptItem.setUniqueAccounts(allUniqueAccounts.size());
            }
        }

        return dappScriptItem;
    }

    private static String generateKey(Integer epochNo, String scriptHash) {
        return format("%s.%d", scriptHash, epochNo);
    }

}
