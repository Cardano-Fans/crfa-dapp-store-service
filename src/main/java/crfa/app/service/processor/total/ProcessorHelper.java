package crfa.app.service.processor.total;

import crfa.app.domain.DappFeed;
import lombok.extern.slf4j.Slf4j;

import java.util.Set;

@Slf4j
public class ProcessorHelper {

    public static long loadMintTransactionsCount(DappFeed dappFeed, String hash) {
        return dappFeed.getMintTransactionsCount().computeIfAbsent(hash, h -> {
            log.warn("Unable to find mint transactions count for hash:{}", h);

            return 0L;
        });
    }

    public static long loadSpendTransactionsCount(DappFeed dappFeed, String hash) {
        return dappFeed.getSpendTransactionsCount().computeIfAbsent(hash, h -> {
            log.warn("Unable to find spend transactions count for hash:{}", h);

            return 0L;
        });
    }

    public static long loadBalance(DappFeed dappFeed, String hash) {
        return dappFeed.getBalance().computeIfAbsent(hash, h -> {
            log.warn("Unable to find balance for hash:{}", h);

            return 0L;
        });
    }

    public static Set<String> loadSpendUniqueAccounts(DappFeed dappFeed, String hash) {
        return dappFeed.getSpendUniqueAccounts().computeIfAbsent(hash, h -> {
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

    public static long loadSpendVolume(DappFeed dappFeed, String hash) {
        return dappFeed.getSpendVolume().computeIfAbsent(hash, h -> {
            log.warn("Unable to load volume for hash:{}", h);
            return 0L;
        });
    }

    public static long loadSpendTrxFee(DappFeed dappFeed, String hash) {
        return dappFeed.getSpendTrxFees().computeIfAbsent(hash, h -> {
            log.warn("Unable to fees for hash:{}", h);
            return 0L;
        });
    }

    public static long loadSpendTrxSize(DappFeed dappFeed, String hash) {
        return dappFeed.getSpendTrxSizes().computeIfAbsent(hash, h -> {
            log.warn("Unable to load trx size for hash:{}", h);
            return 0L;
        });
    }

}
