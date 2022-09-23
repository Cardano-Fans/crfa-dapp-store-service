package crfa.app.service;

import crfa.app.client.metadata.CRFAMetaDataServiceClient;
import crfa.app.domain.DappFeed;
import crfa.app.domain.EpochValue;
import io.vavr.Tuple2;
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
        val tokenHoldersAssetNamesHexToAdaBalanceWithEpoch = loadTokenHoldersBalanceWithEpoch(dataPointers.assetNameHexesToTokenHoldersWithEpoch, scriptLockedPerContractWithEpoch);

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
                .tokenHoldersBalanceEpoch(tokenHoldersAssetNamesHexToAdaBalanceWithEpoch)
                .build();
    }

    private Map<String, Long> loadTokenHoldersBalance(Map<String, Set<String>> assetNameHexesToTokenHolders) {
        // handling special case for WingRiders and when asset based on MintPolicyId has token holders, see: https://github.com/Cardano-Fans/crfa-offchain-data-registry/issues/80
        return assetNameHexesToTokenHolders.entrySet().stream()
                .map(entry -> {
                    val assetId = entry.getKey();
                    val tokenHolderAddresses = entry.getValue();

                    val balanceMap = scrollsOnChainDataService.scriptLocked(tokenHolderAddresses);
                    val adaBalance = balanceMap.values().stream().reduce(0L, Long::sum);

                    return new AbstractMap.SimpleEntry<>(assetId, adaBalance);
                })
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        Map.Entry::getValue
                ));
    }

    private Map<EpochValue<String>, Long> loadTokenHoldersBalanceWithEpoch(
            Map<EpochValue<String>, Set<String>> assetNameHexesToTokenHoldersWithEpoch,
            Map<EpochValue<String>, Long> scriptLockedPerContractWithEpoch) {
        // handling special case for WingRiders and when asset based on MintPolicyId has token holders, see: https://github.com/Cardano-Fans/crfa-offchain-data-registry/issues/80
        return assetNameHexesToTokenHoldersWithEpoch.entrySet().stream()
                .map(entry -> {
                    val assetIdWithEpoch = entry.getKey();
                    val assetId = assetIdWithEpoch.getValue();
                    val epochNo = assetIdWithEpoch.getEpochNo();
                    val addresses = entry.getValue();

                    val balanceMap = addresses.stream().map(addr -> {
                                val epochValue = new EpochValue<>(epochNo, addr);

                                val balance = scriptLockedPerContractWithEpoch.getOrDefault(epochValue, 0L);

                                log.debug("loadTokenHoldersBalanceWithEpoch - addr:{}, balanceAtEpoch:{}, epoch:{}", addr, balance, epochNo);

                                return new Tuple2<>(epochValue, balance);
                            })
                            .collect(Collectors.toMap(
                                    Tuple2::_1,
                                    Tuple2::_2
                            ));

                    // epochNo -> Long
                    val balancePerEpoch = balanceMap.values().stream().reduce(0L, Long::sum);

                    return new AbstractMap.SimpleEntry<>(new EpochValue<>(epochNo, assetId), balancePerEpoch);
                }).collect(Collectors.toMap(
                        Map.Entry::getKey,
                        Map.Entry::getValue
                ));
    }

}
