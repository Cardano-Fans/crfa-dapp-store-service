package crfa.app.service;

import crfa.app.client.metadata.DappSearchItem;
import crfa.app.domain.*;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import lombok.extern.slf4j.Slf4j;
import lombok.val;

import java.util.*;

@Singleton
@Slf4j
public class DataPointsLoader {

    @Inject
    private ScrollsOnChainDataService scrollsOnChainDataService;

    public DataPointers load(List<DappSearchItem> dappSearchResult, InjestionMode injestionMode) {
        val addressPointersList = new HashSet<AddressPointers>();
        val mintPolicyIds = new ArrayList<String>();
        val assetIdToTokenHolders = new HashMap<String, Set<String>>();
        val assetIdToTokenHoldersWithEpoch = new HashMap<EpochKey<String>, Set<String>>();

        dappSearchResult.forEach(dappSearchItem -> {

            dappSearchItem.getReleases().forEach(dappReleaseItem -> {
                val dappReleaseId = new DappReleaseId();

                dappReleaseId.setDappId(dappSearchItem.getId());
                dappReleaseId.setReleaseNumber(dappReleaseItem.getReleaseNumber());

                dappReleaseItem.getScripts().forEach(scriptItem -> {
                    if (scriptItem.getPurpose() == Purpose.MINT) {
                        dappReleaseId.setHash(scriptItem.getMintPolicyID());

                        mintPolicyIds.add(scriptItem.getMintPolicyID());
                        if (scriptItem.getIncludeScriptBalanceFromAsset() != null) {
                            val assetId = scriptItem.getAssetId().get();
                            log.info("Fetching holders for assetId:" + assetId);

                            val tokenHolders = scrollsOnChainDataService.getCurrentAssetHolders(assetId);
                            log.info("got holders count:{}", tokenHolders.size());
                            assetIdToTokenHolders.put(assetId, tokenHolders);

                            if (!(injestionMode == InjestionMode.WITHOUT_EPOCHS_ONLY_AGGREGATES)) {
                                val tokenHoldersWithEpochs = scrollsOnChainDataService.getAssetHoldersWithEpochs(assetId, injestionMode == InjestionMode.CURRENT_EPOCH_AND_AGGREGATES);

                                tokenHoldersWithEpochs.forEach((epochNo, tokenHoldersWith) -> {
                                    assetIdToTokenHoldersWithEpoch.put(new EpochKey<>(epochNo, assetId), tokenHoldersWith);
                                });
                            }
                        }
                    } else if (scriptItem.getPurpose() == Purpose.SPEND) {
                        dappReleaseId.setHash(scriptItem.getScriptHash());
                    }

                    val addressPointer = new AddressPointers();
                    addressPointer.setScriptHash(scriptItem.getScriptHash());

                    if (scriptItem.getPurpose() == Purpose.SPEND) {
                        addressPointer.setContractAddress(scriptItem.getContractAddress());
                    }

                    addressPointersList.add(addressPointer);
                });
            });
        });

        val contractAddresses = addressPointersList.stream()
                .filter(addressPointers -> addressPointers.getContractAddress() != null)
                .map(AddressPointers::getContractAddress)
                .toList();

        val scriptHashes = addressPointersList.stream()
                .filter(addressPointers -> addressPointers.getScriptHash() != null)
                .map(AddressPointers::getScriptHash)
                .toList();

        return DataPointers.builder()
                .addressPointersList(addressPointersList)
                .assetIdToTokenHolders(assetIdToTokenHolders)
                .assetIdToTokenHoldersWithEpoch(assetIdToTokenHoldersWithEpoch)
                .mintPolicyIds(mintPolicyIds)
                .contractAddresses(contractAddresses)
                .scriptHashes(scriptHashes)
                .build();
    }

}
