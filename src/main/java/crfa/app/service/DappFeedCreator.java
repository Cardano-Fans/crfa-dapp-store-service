package crfa.app.service;

import crfa.app.client.blockfrost.BlockfrostAPI;
import crfa.app.client.metadata.CRFAMetaDataServiceClient;
import crfa.app.domain.AddressPointers;
import crfa.app.domain.DappFeed;
import crfa.app.domain.DappReleaseId;
import crfa.app.domain.Purpose;
import crfa.app.repository.DappReleaseItemRepository;
import crfa.app.repository.DappReleasesRepository;
import crfa.app.repository.DappsRepository;
import io.blockfrost.sdk.api.exception.APIException;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import reactor.core.publisher.Mono;

import java.util.*;
import java.util.stream.Collectors;

@Singleton
@Slf4j
public class DappFeedCreator {

    @Inject
    private DappsRepository dappsRepository;

    @Inject
    private DappReleasesRepository dappReleasesRepository;

    @Inject
    private DappReleaseItemRepository dappReleaseItemRepository;

    @Inject
    private CRFAMetaDataServiceClient crfaMetaDataServiceClient;

    @Inject
    private BlockfrostAPI blockfrostAPI;

    @Inject
    private ScrollsOnChainDataService scrollsOnChainDataService;

    @Inject
    private DappService dappService;

    public DappFeed createFeed() {
        log.info("metadata service - fetching all dapps...");
        val dappSearchResult = Mono.from(crfaMetaDataServiceClient.fetchAllDapps()).block();
        log.info("metadata service - fetched all dapps.");

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

        var scriptHashes = addressPointersList.stream()
                .filter(addressPointers -> addressPointers.getScriptHash() != null)
                .map(AddressPointers::getScriptHash)
                .toList();

        var mintPolicyCounts = scrollsOnChainDataService.mintScriptsCount(mintPolicyIds);
        var scriptHashesCount = scrollsOnChainDataService.scriptHashesCount(scriptHashes, true);

        var invocationsCountPerScriptHash = new HashMap<String, Long>();
        invocationsCountPerScriptHash.putAll(mintPolicyCounts);
        invocationsCountPerScriptHash.putAll(scriptHashesCount);

        log.debug("Loading locked per contract address....");
        var scriptLockedPerContract = scrollsOnChainDataService.scriptLocked(contractAddresses);
        log.debug("Loaded locked per contract addresses.");

        log.debug("Loading transaction counts....");
        var trxCounts = scrollsOnChainDataService.transactionsCount(contractAddresses);
        log.debug("Loaded trx counts.");


        // handling special case for WingRiders and when asset based on MintPolicyId has token holders, see: https://github.com/Cardano-Fans/crfa-offchain-data-registry/issues/80
        var tokenHoldersAssetNamesHexToAdaBalance = assetNameHexesToTokenHolders.entrySet().stream()
                .map(e -> {
                    var assetNameHex = e.getKey();
                    var addresses = e.getValue();
                    var balanceMap = scrollsOnChainDataService.scriptLocked(addresses);
                    var adaBalance = balanceMap.values().stream().reduce(0L, Long::sum);

                    return new AbstractMap.SimpleEntry<>(assetNameHex, adaBalance);
                })
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        Map.Entry::getValue
                ));

        return DappFeed.builder()
                .dappSearchResult(dappSearchResult)
                .scriptLockedPerContractAddress(scriptLockedPerContract)
                .invocationsCountPerHash(invocationsCountPerScriptHash)
                .transactionCountsPerContractAddress(trxCounts)
                .tokenHoldersBalance(tokenHoldersAssetNamesHexToAdaBalance)
                .build();
    }


}
