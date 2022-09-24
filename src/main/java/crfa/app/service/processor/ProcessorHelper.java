package crfa.app.service.processor;

import crfa.app.domain.DappFeed;
import crfa.app.domain.EpochKey;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class ProcessorHelper {

    public static long loadInvocationsPerHash(DappFeed dappFeed, String hash) {
        return dappFeed.getInvocationsCountPerHash().computeIfAbsent(hash, h -> {
            log.warn("Unable to find total invocations for hash:{}", h);

            return 0L;
        });
    }

    public static long loadInvocationsPerHash(DappFeed dappFeed, String hash, int epochNo) {
        return dappFeed.getInvocationsCountPerHashEpoch().computeIfAbsent(new EpochKey<>(epochNo, hash), hashEpochKey -> {
            log.warn("Unable to find total invocations for hash:{}", hashEpochKey);

            return 0L;
        });
    }

    public static long loadAddressBalance(DappFeed dappFeed, String contractAddress) {
        return dappFeed.getScriptLockedPerContractAddress().computeIfAbsent(contractAddress, addrEpochKey -> {
            log.warn("Unable to find scriptsLocked for contractAddress:{}", addrEpochKey);

            return 0L;
        });
    }

    public static long loadAddressBalance(DappFeed dappFeed, String contractAddress, int epochNo) {
        return dappFeed.getScriptLockedPerContractAddressEpoch().computeIfAbsent(new EpochKey<>(epochNo, contractAddress), addrEpochKey -> {
            log.warn("Unable to find scriptsLocked for contractAddress:{}", addrEpochKey);

            return 0L;
        });
    }

    public static long loadTransactionsCount(DappFeed dappFeed, String contractAddress) {
        return dappFeed.getTransactionCountsPerContractAddress().computeIfAbsent(contractAddress, addr -> {
            log.warn("Unable to find transactionsCount for contractAddress:{}", addr);

            return 0L;
        });
    }

    public static long loadTransactionsCount(DappFeed dappFeed, String contractAddress, int epochNo) {
        return dappFeed.getTransactionCountsPerContractAddressEpoch().computeIfAbsent(new EpochKey<>(epochNo, contractAddress), addrEpochKey -> {
            log.warn("Unable to find transactionsCount for contractAddress:{}", addrEpochKey);

            return 0L;
        });
    }

    public static long loadTokensBalance(DappFeed dappFeed, String assetId) {
        return dappFeed.getTokenHoldersBalance().computeIfAbsent(assetId, aId -> {
            log.warn("Unable to load balance for assetId:{}", aId);
            return 0L;
        });
    }

    public static long loadTokensBalance(DappFeed dappFeed, String assetId, int epochNo) {
        return dappFeed.getTokenHoldersBalanceEpoch().computeIfAbsent(new EpochKey<>(epochNo, assetId), assetIdEpochKey -> {
            log.warn("Unable to load balance for assetId:{}", assetIdEpochKey);
            return 0L;
        });
    }

    public static long loadVolume(DappFeed dappFeed, String address) {
        // TODO change me to volume stats from scrolls
        return dappFeed.getScriptLockedPerContractAddress().computeIfAbsent(address, addr -> {
            log.warn("Unable to load volume for addr:{}", addr);
            return 0L;
        });
    }

    public static long loadVolume(DappFeed dappFeed, String address, int epochNo) {
        // TODO change me to volume stats from scrolls
        return dappFeed.getScriptLockedPerContractAddressEpoch().computeIfAbsent(new EpochKey<>(epochNo, address), addrEpochKey -> {
            log.warn("Unable to load volume for addrEpochKey:{}", addrEpochKey);
            return 0L;
        });
    }

}
