package crfa.app.service.processor;

import crfa.app.domain.*;
import crfa.app.repository.DappScriptsEpochRepository;
import crfa.app.service.ScrollsOnChainDataService;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import lombok.extern.slf4j.Slf4j;
import lombok.val;

import java.util.ArrayList;
import java.util.Date;

import static crfa.app.domain.EraName.ALONZO;
import static crfa.app.service.processor.ProcessorHelper.*;

@Singleton
@Slf4j
// this handles low level - release items (scripts)
public class DappScriptsFeedEpochProcessor implements FeedProcessor {

    @Inject
    private DappScriptsEpochRepository dappScriptsRepository;

    @Inject
    private ScrollsOnChainDataService scrollsOnChainDataService;

    @Override
    public void process(DappFeed dappFeed) {
        val currentEpochNo = scrollsOnChainDataService.currentEpoch().get();

        val dappScriptItems = new ArrayList<DappScriptItemEpoch>();

        dappFeed.getDappSearchResult().forEach(dappSearchItem -> { // looping over dapps
            dappSearchItem.getReleases().forEach(dappReleaseItem -> { // looping over dapp releases
                for (val scriptItem : dappReleaseItem.getScripts()) { // looping over dapp scripts on release level

                    for (val epochNo : Eras.epochsBetween(ALONZO, currentEpochNo)) {
                        val dappScriptItem = new DappScriptItemEpoch();

                        dappScriptItem.setName(scriptItem.getName());
                        dappScriptItem.setDappId(dappSearchItem.getId());
                        dappScriptItem.setReleaseKey(String.format("%s.%.1f", dappSearchItem.getId(), dappReleaseItem.getReleaseNumber()));
                        dappScriptItem.setVersion(scriptItem.getVersion());
                        dappScriptItem.setUpdateTime(new Date());
                        dappScriptItem.setEpochNo(epochNo);

                        if (scriptItem.getPurpose() == Purpose.SPEND) {
                            val scriptHash = scriptItem.getScriptHash();

                            dappScriptItem.setId(String.format("%s.%d", scriptHash, epochNo));
                            dappScriptItem.setHash(scriptHash);
                            dappScriptItem.setScriptType(ScriptType.SPEND);
                            dappScriptItem.setScriptInvocationsCount(loadInvocationsPerHash(dappFeed, scriptHash, epochNo));

                            val contractAddress = scriptItem.getContractAddress();
                            dappScriptItem.setContractAddress(contractAddress);
                            dappScriptItem.setVolume(Math.abs(loadVolume(dappFeed, contractAddress, epochNo)));
                            dappScriptItem.setTransactionsCount(loadTransactionsCount(dappFeed, contractAddress, epochNo));
                        }
                        if (scriptItem.getPurpose() == Purpose.MINT) {
                            val mintPolicyID = scriptItem.getMintPolicyID();

                            dappScriptItem.setId(String.format("%s.%d", mintPolicyID, epochNo));
                            dappScriptItem.setHash(mintPolicyID);
                            dappScriptItem.setScriptType(ScriptType.MINT);
                            dappScriptItem.setScriptInvocationsCount(loadInvocationsPerHash(dappFeed, mintPolicyID, epochNo));
                            dappScriptItem.setMintPolicyID(scriptItem.getMintPolicyID());
                            dappScriptItem.setClosedEpoch(epochNo != currentEpochNo.intValue());

                            if (scriptItem.getAssetId().isPresent()) {
                                val assetId = scriptItem.getAssetId().get();
                                // in case of purpouse = MINT there is no way we could have any script balance to add, so we only take tokens balance (ADA)
                                dappScriptItem.setVolume(Math.abs(loadVolume(dappFeed, assetId, epochNo)));
                            }
                        }

                        dappScriptItems.add(dappScriptItem);
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

}
