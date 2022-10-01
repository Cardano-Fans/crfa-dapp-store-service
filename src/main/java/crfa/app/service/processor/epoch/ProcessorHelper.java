package crfa.app.service.processor.epoch;

import crfa.app.domain.DappFeed;
import crfa.app.domain.EpochKey;
import lombok.extern.slf4j.Slf4j;

import java.util.Set;

@Slf4j
public class ProcessorHelper {

    public static long loadInvocationsPerHash(DappFeed dappFeed, String hash, int epochNo) {
        return dappFeed.getInvocationsCountPerHashEpoch().computeIfAbsent(new EpochKey<>(epochNo, hash), hashEpochKey -> {
            log.warn("Unable to find total invocations for hash:{}", hashEpochKey);

            return 0L;
        });
    }

    public static long loadAddressBalance(DappFeed dappFeed, String contractAddress, int epochNo) {
        return dappFeed.getScriptLockedPerContractAddressEpoch().computeIfAbsent(new EpochKey<>(epochNo, contractAddress), addrEpochKey -> {
            log.warn("Unable to find scriptsLocked for contractAddress:{}", addrEpochKey);

            return 0L;
        });
    }

    public static long loadTransactionsCount(DappFeed dappFeed, String contractAddress, int epochNo) {
        return dappFeed.getTransactionCountsPerContractAddressEpoch().computeIfAbsent(new EpochKey<>(epochNo, contractAddress), addrEpochKey -> {
            log.warn("Unable to find transactionsCount for contractAddress:{}", addrEpochKey);

            return 0L;
        });
    }

    public static Set<String> loadUniqueAccounts(DappFeed dappFeed, String contractAddress, int epochNo) {
        return dappFeed.getUniqueAccountsEpoch().computeIfAbsent(new EpochKey<>(epochNo, contractAddress), addrEpochKey -> {
            log.warn("Unable to find unique addresses for contractAddress:{}", addrEpochKey);

            return Set.of();
        });
    }

    public static long loadTokensBalance(DappFeed dappFeed, String assetId, int epochNo) {
        return dappFeed.getTokenHoldersBalanceEpoch().computeIfAbsent(new EpochKey<>(epochNo, assetId), assetIdEpochKey -> {
            log.warn("Unable to load balance for assetId:{}", assetIdEpochKey);
            return 0L;
        });
    }

    public static long loadVolume(DappFeed dappFeed, String address, int epochNo) {
        return dappFeed.getVolumePerContractAddressEpoch().computeIfAbsent(new EpochKey<>(epochNo, address), addrEpochKey -> {
            log.warn("Unable to load volume for addrEpochKey:{}", addrEpochKey);
            return 0L;
        });
    }

}
