package crfa.app.service;

import crfa.app.client.metadata.DappSearchItem;
import crfa.app.domain.*;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import lombok.extern.slf4j.Slf4j;
import lombok.val;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Set;

import static crfa.app.domain.InjestionMode.CURRENT_EPOCH_AND_AGGREGATES;

@Singleton
@Slf4j
public class DataPointsLoader {

    @Inject
    private ScrollsOnChainDataService scrollsOnChainDataService;

    public DataPointers load(List<DappSearchItem> dappSearchResult, InjestionMode injestionMode) {
        val scriptHashes = new ArrayList<String>();
        val mintPolicyIds = new ArrayList<String>();

        val assetIdToTokenHolders = new HashMap<String, Set<String>>();
        val assetIdToTokenHoldersWithEpoch = new HashMap<EpochKey<String>, Set<String>>();

        dappSearchResult.forEach(dappSearchItem -> {
            dappSearchItem.getReleases().forEach(dappReleaseItem -> {
                val dappReleaseId = new DappReleaseId();

                dappReleaseId.setDappId(dappSearchItem.getId());
                dappReleaseId.setReleaseNumber(dappReleaseItem.getReleaseNumber());

                dappReleaseItem.getScripts().forEach(scriptItem -> {

                    if (scriptItem.getPurpose() == Purpose.SPEND) {
                        scriptHashes.add(scriptItem.getUnifiedHash());
                    }

                    if (scriptItem.getPurpose() == Purpose.MINT) {
                        mintPolicyIds.add(scriptItem.getMintPolicyID());

                        if (scriptItem.getIncludeScriptBalanceFromAsset() != null) {
                            val assetId = scriptItem.getAssetId().orElseThrow();
                            log.info("Fetching holders for assetId:" + assetId);

                            val tokenHolders = scrollsOnChainDataService.getCurrentAssetHolders(assetId);
                            log.info("got holders count:{}", tokenHolders.size());
                            assetIdToTokenHolders.put(assetId, tokenHolders);

                            if (!(injestionMode == InjestionMode.WITHOUT_EPOCHS_ONLY_AGGREGATES)) {
                                val tokenHoldersWithEpochs = scrollsOnChainDataService.getAssetHoldersWithEpochs(assetId, injestionMode == CURRENT_EPOCH_AND_AGGREGATES);

                                tokenHoldersWithEpochs.forEach((epochNo, tokenHoldersWith) -> {
                                    assetIdToTokenHoldersWithEpoch.put(new EpochKey<>(epochNo, assetId), tokenHoldersWith);
                                });
                            }
                        }
                    }
                });
            });
        });

        return DataPointers.builder()
                .mintPolicyIds(mintPolicyIds)
                .scriptHashes(scriptHashes)
                .assetIdToTokenHolders(assetIdToTokenHolders)
                .assetIdToTokenHoldersWithEpoch(assetIdToTokenHoldersWithEpoch)
                .build();
    }

}
