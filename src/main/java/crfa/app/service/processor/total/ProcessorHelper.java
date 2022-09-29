package crfa.app.service.processor.total;

import crfa.app.domain.DappFeed;
import lombok.extern.slf4j.Slf4j;

import java.util.Set;

@Slf4j
public class ProcessorHelper {

    public static long loadInvocationsPerHash(DappFeed dappFeed, String hash) {
        return dappFeed.getInvocationsCountPerHash().computeIfAbsent(hash, h -> {
            log.warn("Unable to find total invocations for hash:{}", h);

            return 0L;
        });
    }

    public static long loadAddressBalance(DappFeed dappFeed, String contractAddress) {
        return dappFeed.getScriptLockedPerContractAddress().computeIfAbsent(contractAddress, addrEpochKey -> {
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

    public static Set<String> loadUniqueAccounts(DappFeed dappFeed, String contractAddress) {
        return dappFeed.getUniqueAccounts().computeIfAbsent(contractAddress, addr -> {
            log.warn("Unable to find unique addresses for contractAddress:{}", addr);

            return Set.of();
        });
    }

    public static long loadTokensBalance(DappFeed dappFeed, String assetId) {
        return dappFeed.getTokenHoldersBalance().computeIfAbsent(assetId, aId -> {
            log.warn("Unable to load balance for assetId:{}", aId);
            return 0L;
        });
    }

    public static long loadVolume(DappFeed dappFeed, String address) {
        return dappFeed.getVolumePerContractAddress().computeIfAbsent(address, addr -> {
            log.warn("Unable to load volume for addr:{}", addr);
            return 0L;
        });
    }

}
