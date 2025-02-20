package crfa.app.service.processor.epoch;

import crfa.app.client.metadata.DappReleaseItem;
import crfa.app.client.metadata.DappSearchItem;
import crfa.app.client.metadata.ScriptItem;
import crfa.app.domain.*;
import crfa.app.repository.epoch.DappScriptsEpochRepository;
import crfa.app.service.ScrollsOnChainDataService;
import crfa.app.service.processor.FeedProcessor;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import lombok.extern.slf4j.Slf4j;
import lombok.val;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;

import static crfa.app.domain.Purpose.MINT;
import static crfa.app.domain.Purpose.SPEND;
import static crfa.app.service.processor.epoch.ProcessorHelper.*;
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
    public boolean isEpochProcessor() {
        return true;
    }

    @Override
    public void process(DappFeed dappFeed, InjestionMode injestionMode) {
        if (injestionMode == InjestionMode.WITHOUT_EPOCHS_ONLY_AGGREGATES) {
            return;
        }

        val dappScriptItems = new ArrayList<DappScriptItemEpoch>();
        val currentEpochNo = scrollsOnChainDataService.currentEpoch().orElseThrow();

        dappFeed.getDappSearchResult().forEach(dappSearchItem -> { // looping over dapps
            dappSearchItem.getReleases().forEach(dappReleaseItem -> { // looping over dapp releases
                for (val scriptItem : dappReleaseItem.getScripts()) { // looping over dapp scripts on release level
                    val injestCurrentEpochOnly = injestionMode == InjestionMode.CURRENT_EPOCH_AND_AGGREGATES;

                    if (injestCurrentEpochOnly) {
                        val dappItemEpoch = createDappItemEpoch(dappFeed, false, dappSearchItem, dappReleaseItem, scriptItem, currentEpochNo);
                        dappScriptItems.add(dappItemEpoch);
                        return;
                    }

                    for (val epochNo : Eras.epochsBetween(SnapshotType.ALL.startEpoch(currentEpochNo), currentEpochNo)) {
                        val isClosedEpoch = epochNo < currentEpochNo;
                        val dappItemEpoch = createDappItemEpoch(dappFeed, isClosedEpoch, dappSearchItem, dappReleaseItem, scriptItem, epochNo);
                        dappScriptItems.add(dappItemEpoch);
                    }
                }
            });
        });

        log.info("Upserting dapp script item epochs..., itemsCount:{}", dappScriptItems.size());

        dappScriptItems.forEach(dappScriptItemEpoch -> dappScriptsRepository.update(dappScriptItemEpoch));

        log.info("Upserted dapp script item epochs.");

        dappScriptsRepository.removeAllExcept(dappScriptItems);
    }

    private static DappScriptItemEpoch createDappItemEpoch(DappFeed dappFeed,
                                                           boolean isClosedEpoch,
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
        dappScriptItem.setPlutusVersion(scriptItem.getPlutusVersion());

        dappScriptItem.setClosedEpoch(isClosedEpoch);

        val hash = scriptItem.getUnifiedHash();

        val allUniqueAccounts = new HashSet<>();

        dappScriptItem.setId(generateKey(epochNo, hash));
        dappScriptItem.setHash(hash);

        if (scriptItem.getPurpose() == SPEND) {
            dappScriptItem.setScriptType(ScriptType.SPEND);
            dappScriptItem.setTransactions(loadSpendTransactionsCount(dappFeed, hash, epochNo));
            dappScriptItem.setInflowsOutflows(loadBalance(dappFeed, hash, epochNo));
            dappScriptItem.setVolume(loadSpendVolume(dappFeed, hash, epochNo));
            dappScriptItem.setTrxFees(loadSpendTrxFee(dappFeed, hash, epochNo));
            dappScriptItem.setTrxSizes(loadTrxSize(dappFeed, hash, epochNo));

            allUniqueAccounts.addAll(loadSpendUniqueAccounts(dappFeed, hash, epochNo));
        }

        if (scriptItem.getPurpose() == MINT) {
            dappScriptItem.setScriptType(ScriptType.MINT);
            dappScriptItem.setMintPolicyID(scriptItem.getMintPolicyID());
            dappScriptItem.setTransactions(loadMintTransactionsCount(dappFeed, hash, epochNo));

            if (scriptItem.getAssetId().isPresent()) {
                val assetId = scriptItem.getAssetId().orElseThrow();
                // in case of purpouse = MINT there is no way we could have any script balance to add, so we only take tokens balance (ADA)
                dappScriptItem.setInflowsOutflows(dappScriptItem.getInflowsOutflows() == null ? 0 : dappScriptItem.getInflowsOutflows() + loadTokensBalance(dappFeed, assetId, epochNo));
            }
        }

        dappScriptItem.setUniqueAccounts(allUniqueAccounts.size());

        return dappScriptItem;
    }

    private static String generateKey(Integer epochNo, String scriptHash) {
        return format("%s.%d", scriptHash, epochNo);
    }

}
