package crfa.app.service;

import crfa.app.client.metadata.CRFAMetaDataServiceClient;
import crfa.app.domain.DappFeed;
import crfa.app.domain.EpochKey;
import crfa.app.domain.InjestionMode;
import io.vavr.Tuple2;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import reactor.core.publisher.Mono;

import java.util.AbstractMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Singleton
@Slf4j

public class DappFeedCreator {

    @Inject
    private DataPointsLoader dataPointsLoader;

    @Inject
    private CRFAMetaDataServiceClient crfaMetaDataServiceClient;

    @Inject
    private ScrollsOnChainDataService scrollsOnChainDataService;

    public DappFeed createFeed(InjestionMode injestionMode) {
        log.info("metadata service - fetching all dapps...");
        val dappSearchResult = Mono.from(crfaMetaDataServiceClient.fetchAllDapps()).block();
        log.info("metadata service - fetched all dapps.");

        val dataPointers = dataPointsLoader.load(dappSearchResult, injestionMode);

        val mintPolicyCounts = scrollsOnChainDataService.mintTransactionsCount(dataPointers.mintPolicyIds);
        val spendTransactionsCount = scrollsOnChainDataService.spendTransactionsCount(dataPointers.scriptHashes);

        val spendAdaBalance = scrollsOnChainDataService.balance(dataPointers.scriptHashes);
        val spendVolume = scrollsOnChainDataService.spendVolume(dataPointers.scriptHashes);
        val tokenHoldersAssetIdToAdaBalance = loadTokenHoldersBalance(dataPointers.assetIdToTokenHolders);
        val spendFees = scrollsOnChainDataService.spendFees(dataPointers.scriptHashes);
        val spendUniqueAccounts = scrollsOnChainDataService.spendUniqueAccounts(dataPointers.scriptHashes);
        val spendTrxSizes = scrollsOnChainDataService.spendTrxSizes(dataPointers.scriptHashes);
        val poolHexes = scrollsOnChainDataService.poolHexes(dataPointers.scriptHashes);

        val uniqueAccountsMerge = uniqueAccountsUnion(spendUniqueAccounts, dataPointers.assetIdToTokenHolders);

        val isCurrentEpochAndAggregates = false;

        val scriptLockedPerContractWithEpoch = scrollsOnChainDataService.balanceWithEpoch(dataPointers.scriptHashes, isCurrentEpochAndAggregates);
        val volumePerContractWithEpoch = scrollsOnChainDataService.spendVolumeEpochLevel(dataPointers.scriptHashes, isCurrentEpochAndAggregates);
        val mintPolicyCountsWithEpoch = scrollsOnChainDataService.mintTransactionsCountWithEpoch(dataPointers.mintPolicyIds, isCurrentEpochAndAggregates);
        val spendTransactionsCountWithEpoch = scrollsOnChainDataService.spendTransactionsCountEpoch(dataPointers.scriptHashes, isCurrentEpochAndAggregates);
        val tokenHoldersAssetIdToAdaBalanceWithEpoch = loadTokenHoldersBalanceWithEpoch(dataPointers.assetIdToTokenHoldersWithEpoch, scriptLockedPerContractWithEpoch, isCurrentEpochAndAggregates);
        val uniqueAccountsWithEpoch = scrollsOnChainDataService.spendUniqueAccountsWithEpoch(dataPointers.scriptHashes, isCurrentEpochAndAggregates);
        val feesWithEpoch = scrollsOnChainDataService.spendFeesEpochLevel(dataPointers.scriptHashes, isCurrentEpochAndAggregates);
        val trxSizesWithEpoch = scrollsOnChainDataService.spendTrxSizesWithEpoch(dataPointers.scriptHashes, isCurrentEpochAndAggregates);

        val uniqueAccountsMergeEpoch = uniqueAccountsUnionEpoch(uniqueAccountsWithEpoch, dataPointers.assetIdToTokenHoldersWithEpoch);

        return DappFeed.builder()
                .dappSearchResult(dappSearchResult)

                // for all epochs - aggregates
                .balance(spendAdaBalance)
                .spendVolume(spendVolume)
                .spendTrxFees(spendFees)
                .spendTrxSizes(spendTrxSizes)
                .poolHexes(poolHexes)
                .mintTransactionsCount(mintPolicyCounts)
                .spendTransactionsCount(spendTransactionsCount)
                .spendUniqueAccounts(uniqueAccountsMerge)
                .tokenHoldersBalance(tokenHoldersAssetIdToAdaBalance)
                .tokenHoldersAddresses(dataPointers.assetIdToTokenHolders)

                // epoch level
                .balanceEpoch(scriptLockedPerContractWithEpoch)
                .mintTransactionsCountEpoch(mintPolicyCountsWithEpoch)
                .spendTransactionCountEpoch(spendTransactionsCountWithEpoch)
                .tokenHoldersBalanceEpoch(tokenHoldersAssetIdToAdaBalanceWithEpoch)
                .spendVolumeEpoch(volumePerContractWithEpoch)
                .spendTrxFeesEpoch(feesWithEpoch)
                .spendTrxSizesEpoch(trxSizesWithEpoch)
                .spendUniqueAccountsEpoch(uniqueAccountsMergeEpoch)
                .tokenHoldersAddressesEpoch(dataPointers.assetIdToTokenHoldersWithEpoch)
                .build();
    }

    private Map<String, Long> loadTokenHoldersBalance(Map<String, Set<String>> assetIdToTokenHolders) {
        // handling special case for WingRiders and when asset based on MintPolicyId has token holders, see: https://github.com/Cardano-Fans/crfa-offchain-data-registry/issues/80
        return assetIdToTokenHolders.entrySet().stream()
                .map(entry -> {
                    val assetId = entry.getKey();
                    val tokenHolderAddresses = entry.getValue();

                    val balanceMap = scrollsOnChainDataService.balance(tokenHolderAddresses);
                    val adaBalance = balanceMap.values().stream().reduce(0L, Long::sum);

                    return new AbstractMap.SimpleEntry<>(assetId, adaBalance);
                })
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        Map.Entry::getValue
                ));
    }

    private Map<EpochKey<String>, Long> loadTokenHoldersBalanceWithEpoch(
            Map<EpochKey<String>, Set<String>> assetIdToTokenHoldersWithEpoch,
            Map<EpochKey<String>, Long> scriptLockedPerContractWithEpoch,
            boolean isCurrentEpochOnly) {
        // handling special case for WingRiders and when asset based on MintPolicyId has token holders, see: https://github.com/Cardano-Fans/crfa-offchain-data-registry/issues/80

        val currentEpoch = scrollsOnChainDataService.currentEpoch().orElseThrow();

        return assetIdToTokenHoldersWithEpoch.entrySet().stream()
                .filter(p -> isCurrentEpochOnly ? p.getKey().getEpochNo() == currentEpoch : true)
                .map(entry -> {
                    val assetIdWithEpoch = entry.getKey();
                    val assetId = assetIdWithEpoch.getValue();
                    val epochNo = assetIdWithEpoch.getEpochNo();
                    val addresses = entry.getValue();

                    val balanceMap = addresses.stream().map(addr -> {
                        val epochKey = new EpochKey<>(epochNo, addr);

                        val balance = scriptLockedPerContractWithEpoch.getOrDefault(epochKey, 0L);

                        return new Tuple2<>(epochKey, balance);
                    })
                    .collect(Collectors.toMap(
                            Tuple2::_1,
                            Tuple2::_2
                    ));

                    // epochNo -> Long
                    val balancePerEpoch = balanceMap.values().stream().reduce(0L, Long::sum);

                    return new AbstractMap.SimpleEntry<>(new EpochKey<>(epochNo, assetId), balancePerEpoch);
                }).collect(Collectors.toMap(
                        Map.Entry::getKey,
                        Map.Entry::getValue
                ));
    }

    private Map<String, Set<String>> uniqueAccountsUnion(Map<String, Set<String>> a, Map<String, Set<String>> b) {
        return Stream.concat(a.entrySet().stream(), b.entrySet().stream())
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        Map.Entry::getValue,
                        (accs1, accs2) -> {
                            val s = new HashSet<String>();
                            s.addAll(accs1);
                            s.addAll(accs2);

                            return s;
                        }));
    }

    private Map<EpochKey<String>, Set<String>> uniqueAccountsUnionEpoch(Map<EpochKey<String>, Set<String>> a, Map<EpochKey<String>, Set<String>> b) {
        return Stream.concat(a.entrySet().stream(), b.entrySet().stream())
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        Map.Entry::getValue,
                        (accs1, accs2) -> {
                            val s = new HashSet<String>();
                            s.addAll(accs1);
                            s.addAll(accs2);

                            return s;
                        }));
    }

}
