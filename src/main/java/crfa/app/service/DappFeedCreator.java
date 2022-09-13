package crfa.app.service;

import com.google.common.collect.Maps;
import crfa.app.client.metadata.CRFAMetaDataServiceClient;
import crfa.app.domain.DappFeed;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import reactor.core.publisher.Mono;

import java.util.AbstractMap;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import static crfa.app.utils.MoreMaps.addMaps;

@Singleton
@Slf4j

public class DappFeedCreator {

    @Inject
    private DataPointsLoader dataPointsLoader;

    @Inject
    private CRFAMetaDataServiceClient crfaMetaDataServiceClient;

    @Inject
    private ScrollsOnChainDataService scrollsOnChainDataService;

    public DappFeed createFeed() {
        log.info("metadata service - fetching all dapps...");
        val dappSearchResult = Mono.from(crfaMetaDataServiceClient.fetchAllDapps()).block();
        log.info("metadata service - fetched all dapps.");

        val dataPointers = dataPointsLoader.load(dappSearchResult);

        val mintPolicyCounts = scrollsOnChainDataService.mintScriptsCount(dataPointers.mintPolicyIds);

        val scriptHashesCount = scrollsOnChainDataService.scriptHashesCount(dataPointers.scriptHashes, true);

        log.debug("Loading locked per contract address....");
        val scriptLockedPerContractAddr = scrollsOnChainDataService.scriptLocked(dataPointers.contractAddresses);
        log.debug("Loaded locked per contract addresses.");

        log.debug("Loading transaction counts....");
        val transactionsCountPerContractAddr = scrollsOnChainDataService.transactionsCount(dataPointers.contractAddresses);
        log.debug("Loaded transaction counts.");

        val tokenHoldersAssetNamesHexToAdaBalance = loadTokenHoldersBalance(dataPointers.assetNameHexesToTokenHolders);

        val scriptLockedPerContractWithEpoch = scrollsOnChainDataService.scriptLockedWithEpochs(dataPointers.contractAddresses);
        val transactionsCountPerContractWithEpoch = scrollsOnChainDataService.transactionsCountWithEpochs(dataPointers.contractAddresses);
        val mintPolicyCountsWithEpoch = scrollsOnChainDataService.mintScriptsCountWithEpochs(dataPointers.mintPolicyIds);
        val scriptHashesCountWithEpoch = scrollsOnChainDataService.scriptHashesCountWithEpochs(dataPointers.scriptHashes, true);

        return DappFeed.builder()
                .dappSearchResult(dappSearchResult)

                // for all epochs
                .scriptLockedPerContractAddress(scriptLockedPerContractAddr)
                .invocationsCountPerHash(addMaps(mintPolicyCounts, scriptHashesCount))
                .transactionCountsPerContractAddress(transactionsCountPerContractAddr)
                .tokenHoldersBalance(tokenHoldersAssetNamesHexToAdaBalance)

                // epoch level
                .scriptLockedPerContractAddressEpoch(scriptLockedPerContractWithEpoch)
                .transactionCountsPerContractAddressEpoch(transactionsCountPerContractWithEpoch)
                .invocationsCountPerHashEpoch(addMaps(mintPolicyCountsWithEpoch, scriptHashesCountWithEpoch))
                .tokenHoldersBalance(Maps.newHashMap()) // TODO - implement
                .build();
    }

    private Map<String, Long> loadTokenHoldersBalance(Map<String, Set<String>> assetNameHexesToTokenHolders) {
        // handling special case for WingRiders and when asset based on MintPolicyId has token holders, see: https://github.com/Cardano-Fans/crfa-offchain-data-registry/issues/80
        return assetNameHexesToTokenHolders.entrySet().stream()
                .map(entry -> {
                    val assetNameHex = entry.getKey();
                    val addresses = entry.getValue();

                    val balanceMap = scrollsOnChainDataService.scriptLocked(addresses);
                    val adaBalance = balanceMap.values().stream().reduce(0L, Long::sum);

                    return new AbstractMap.SimpleEntry<>(assetNameHex, adaBalance);
                })
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        Map.Entry::getValue
                ));
    }

//    private Map<EpochValue<String>, Long> loadTokenHoldersBalanceWithEpoch(Map<String, Set<String>> assetNameHexesToTokenHolders) {
//        // handling special case for WingRiders and when asset based on MintPolicyId has token holders, see: https://github.com/Cardano-Fans/crfa-offchain-data-registry/issues/80
//        return assetNameHexesToTokenHolders.entrySet().stream()
//                .map(entry -> {
//                    val assetNameHex = entry.getKey();
//                    val addresses = entry.getValue();
//                    val balanceMap = scrollsOnChainDataService.scriptLockedWithEpochs(addresses);
//
//                    // epochNo -> Long
//                    val balancePerEpoch = reduceAddresses(balanceMap);
//
//                    return new AbstractMap.SimpleEntry<>(assetNameHex, balancePerEpoch);
//                })
//                .collect(Collectors.toMap(
//                        Map.Entry::getKey,
//                        Map.Entry::getValue
//                ));
//    }
//
//    private Map<Integer, Long> reduceAddresses(Map<EpochValue<String>, Long> data) {
//
//    }

}
