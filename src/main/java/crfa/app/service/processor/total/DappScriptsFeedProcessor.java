package crfa.app.service.processor.total;

import crfa.app.domain.*;
import crfa.app.repository.PoolRepository;
import crfa.app.repository.total.DappScriptsRepository;
import crfa.app.service.processor.FeedProcessor;
import crfa.app.utils.Json;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import lombok.extern.slf4j.Slf4j;
import lombok.val;

import java.util.ArrayList;
import java.util.Date;
import java.util.Map;

import static crfa.app.domain.Purpose.MINT;
import static crfa.app.domain.Purpose.SPEND;
import static crfa.app.service.processor.total.ProcessorHelper.*;

@Singleton
@Slf4j
// DapppReleasesFeedProcessor handles low level - release items (scripts)
public class DappScriptsFeedProcessor implements FeedProcessor {

    @Inject
    private DappScriptsRepository dappScriptsRepository;

    @Inject
    private PoolRepository poolRepository;

    @Inject
    private Json json;

    @Override
    public void process(DappFeed dappFeed, InjestionMode injestionMode, FeedProcessingContext context) {
        val dappScriptItems = new ArrayList<DappScriptItem>();

        dappFeed.getDappSearchResult().forEach(dappSearchItem -> { // looping over dapps
            for (val dappReleaseItem : dappSearchItem.getReleases()) {// looping over dapp releases
                for (val scriptItem : dappReleaseItem.getScripts()) { // looping over dapp scripts on release level
                    val newDappReleaseItem = new DappScriptItem();
                    val releaseKey = String.format("%s.%.1f", dappSearchItem.getId(), dappReleaseItem.getReleaseNumber());

                    newDappReleaseItem.setName(scriptItem.getName());
                    newDappReleaseItem.setDappId(dappSearchItem.getId());
                    newDappReleaseItem.setReleaseKey(releaseKey);
                    newDappReleaseItem.setVersion(scriptItem.getVersion());
                    newDappReleaseItem.setPlutusVersion(scriptItem.getPlutusVersion());
                    newDappReleaseItem.setUpdateTime(new Date());

                    val hash = scriptItem.getUnifiedHash();
                    newDappReleaseItem.setHash(hash);
                    newDappReleaseItem.setScriptType(scriptItem.getPurpose() == SPEND ? ScriptType.SPEND : ScriptType.MINT);

                    if (scriptItem.getPurpose() == SPEND) {
                        newDappReleaseItem.setScriptType(ScriptType.SPEND);
                        newDappReleaseItem.setBalance(loadBalance(dappFeed, hash));
                        newDappReleaseItem.setVolume(loadSpendVolume(dappFeed, hash));
                        newDappReleaseItem.setTrxFees(loadSpendTrxFee(dappFeed, hash));
                        newDappReleaseItem.setTrxSizes(loadSpendTrxSize(dappFeed, hash));
                        newDappReleaseItem.setTransactions(loadSpendTransactionsCount(dappFeed, hash));

                        newDappReleaseItem.setUniqueAccounts(loadSpendUniqueAccounts(dappFeed, hash).size());

                        setPoolDataSpendType(dappFeed, newDappReleaseItem, hash);
                    }

                    if (scriptItem.getPurpose() == MINT) {
                        newDappReleaseItem.setScriptType(ScriptType.MINT);
                        val mintPolicyID = scriptItem.getMintPolicyID();
                        newDappReleaseItem.setMintPolicyID(mintPolicyID);
                        newDappReleaseItem.setTransactions(loadMintTransactionsCount(dappFeed, hash));

                        newDappReleaseItem.setPool(json.write(Map.of("status", PoolStatus.NA)));

                        // e.g. wing riders
                        if (scriptItem.getAssetId().isPresent()) {
                            val assetId = scriptItem.getAssetId().orElseThrow();
                            // in case of purpouse = MINT there is no way we could have any script balance to add, so we only take tokens balance (ADA)

                            // ???????????????
                            newDappReleaseItem.setBalance(loadTokensBalance(dappFeed, assetId));
                        }
                    }

                    dappScriptItems.add(newDappReleaseItem);
                }
            }
        });

        log.info("Upserting dapp script items..., itemsCount:{}", dappScriptItems.size());
        dappScriptItems.forEach(dappScriptItem -> dappScriptsRepository.update(dappScriptItem));
        log.info("Upserted dapp script items.");

        dappScriptsRepository.removeAllExcept(dappScriptItems);
    }

    private void setPoolDataSpendType(DappFeed dappFeed, DappScriptItem dappScriptItem, String hash) {
        val poolsCount = poolRepository.poolCount();
        if (poolsCount == 0) {
            dappScriptItem.setPool(json.write(Map.of("status", PoolStatus.SYNC_IN_PROGRESS)));
            return;
        }

        loadPoolHex(dappFeed, hash).ifPresent(poolE -> {
            if (poolE.isLeft()) {
                switch (poolE.getLeft()) {
                    case NOT_FOUND -> dappScriptItem.setPool(json.write(Map.of("status", PoolStatus.NOT_FOUND)));
                    case NOT_STAKED -> dappScriptItem.setPool(json.write(Map.of("status", PoolStatus.NOT_STAKED)));
                }
            }

            if (poolE.isRight()) {
                val poolHex = poolE.get();

                poolRepository.findById(poolHex).ifPresentOrElse(pool -> {
                    dappScriptItem.setPool(json.write(Map.of(
                            "status", PoolStatus.STAKED,
                            "pool", pool)
                    ));
                }, () -> {
                    dappScriptItem.setPool(json.write(Map.of("status", PoolStatus.SYNC_IN_PROGRESS)));
                });
            }
        });
    }

}
