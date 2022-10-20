package crfa.app.service.processor.total;

import crfa.app.domain.DappFeed;
import lombok.extern.slf4j.Slf4j;

import java.util.Set;

@Slf4j
public class ProcessorHelper {

    public static long loadInvocations(DappFeed dappFeed, String hash) {
        return dappFeed.getInvocationsCount().computeIfAbsent(hash, h -> {
            log.warn("Unable to find total invocations for hash:{}", h);

            return 0L;
        });
    }

    public static long loadAdaBalance(DappFeed dappFeed, String hash) {
        return dappFeed.getGetAdaBalance().computeIfAbsent(hash, h -> {
            log.warn("Unable to find scriptsLocked for hash:{}", h);

            return 0L;
        });
    }

    public static Set<String> loadUniqueAccounts(DappFeed dappFeed, String hash) {
        return dappFeed.getUniqueAccounts().computeIfAbsent(hash, h -> {
            log.warn("Unable to find unique addresses for hash:{}", h);

            return Set.of();
        });
    }

    public static long loadTokensBalance(DappFeed dappFeed, String assetId) {
        return dappFeed.getTokenHoldersBalance().computeIfAbsent(assetId, aId -> {
            log.warn("Unable to load balance for assetId:{}", aId);
            return 0L;
        });
    }

    public static long loadVolume(DappFeed dappFeed, String hash) {
        return dappFeed.getVolume().computeIfAbsent(hash, h -> {
            log.warn("Unable to load volume for hash:{}", h);
            return 0L;
        });
    }

    public static long loadFees(DappFeed dappFeed, String hash) {
        return dappFeed.getFees().computeIfAbsent(hash, h -> {
            log.warn("Unable to fees for hash:{}", h);
            return 0L;
        });
    }

}
