package crfa.app.service;

import crfa.app.client.blockfrost.BlockfrostAPI;
import crfa.app.client.metadata.DappSearchItem;
import crfa.app.domain.AddressPointers;
import crfa.app.domain.DappReleaseId;
import crfa.app.domain.DataPointers;
import crfa.app.domain.Purpose;
import io.blockfrost.sdk.api.exception.APIException;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import lombok.extern.slf4j.Slf4j;
import lombok.val;

import java.util.*;

@Singleton
@Slf4j
public class DataPointsLoader {

    @Inject
    private BlockfrostAPI blockfrostAPI;

    public DataPointers load(List<DappSearchItem> dappSearchResult) {
        val addressPointersList = new HashSet<AddressPointers>();
        val mintPolicyIds = new ArrayList<String>();
        val assetNameHexesToTokenHolders = new HashMap<String, Set<String>>();

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
                            try {
                                val assetNameHex = scriptItem.getAssetNameAsHex().get();
                                log.info("Fetching holders for assetNameHex:" + assetNameHex);

                                val tokenHolders = blockfrostAPI.tokenHolders(assetNameHex);

                                log.info("got holders count:{}", tokenHolders.size());
                                assetNameHexesToTokenHolders.put(assetNameHex, tokenHolders);
                            } catch (APIException e) {
                                throw new RuntimeException("blockfrost exception, unable to fetch token holders", e);
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
                .assetNameHexesToTokenHolders(assetNameHexesToTokenHolders)
                .mintPolicyIds(mintPolicyIds)
                .contractAddresses(contractAddresses)
                .scriptHashes(scriptHashes)
                .build();
    }

}
