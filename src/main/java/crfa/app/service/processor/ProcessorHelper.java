package crfa.app.service.processor;

import crfa.app.domain.DappFeed;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class ProcessorHelper {

    public static long loadInvocationsPerHash(DappFeed dappFeed, String scriptHash) {
        return dappFeed.getInvocationsCountPerHash().computeIfAbsent(scriptHash, hash -> {
            log.warn("Unable to find total invocations for scriptHash:{}", hash);

            return 0L;
        });
    }

    public static long loadInvocationsCountPerHash(DappFeed dappFeed, String mintPolicyID) {
        return dappFeed.getInvocationsCountPerHash().computeIfAbsent(mintPolicyID, hash -> {
            log.warn("Unable to find invocationsPerHash hash:{}", hash);
            return 0L;
        });
    }

    public static long loadAddressBalance(DappFeed dappFeed, String contractAddress) {
        return dappFeed.getScriptLockedPerContractAddress().computeIfAbsent(contractAddress, addr -> {
            log.warn("Unable to find scriptsLocked for contractAddress:{}", addr);

            return 0L;
        });
    }

    public static long loadTransactionsCount(DappFeed dappFeed, String contractAddress) {
        return dappFeed.getTransactionCountsPerContractAddress().computeIfAbsent(contractAddress, addr -> {
            log.warn("Unable to find transactionsCount for contractAddress:{}", addr);

            return 0L;
        });
    }

    public static long loadTokensBalance(DappFeed dappFeed, String assetId) {
        return dappFeed.getTokenHoldersBalance().computeIfAbsent(assetId, aId -> {
            log.warn("Unable to load balance for assetId:{}", assetId);
            return 0L;
        });
    }

}
