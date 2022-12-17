package crfa.app.service.processor.epoch;

import crfa.app.domain.DappFeed;
import crfa.app.domain.EpochKey;
import lombok.extern.slf4j.Slf4j;

import java.util.Set;

@Slf4j
public class ProcessorHelper {

    public static long loadSpendTransactionsCount(DappFeed dappFeed, String hash, int epochNo) {
        return dappFeed.getSpendTransactionCountEpoch().computeIfAbsent(new EpochKey<>(epochNo, hash), hashEpochKey -> {
            log.warn("Unable to find spend transactions count for hashEpochKey:{}", hashEpochKey);

            return 0L;
        });
    }

    public static long loadMintTransactionsCount(DappFeed dappFeed, String hash, int epochNo) {
        return dappFeed.getMintTransactionsCountEpoch().computeIfAbsent(new EpochKey<>(epochNo, hash), hashEpochKey -> {
            log.warn("Unable to find mint transactions count for hashEpochKey:{}", hashEpochKey);

            return 0L;
        });
    }

    public static long loadBalance(DappFeed dappFeed, String hash, int epochNo) {
        return dappFeed.getBalanceEpoch().computeIfAbsent(new EpochKey<>(epochNo, hash), hashEpochKey -> {
            log.warn("Unable to find scriptsLocked for hashEpochKey:{}", hashEpochKey);

            return 0L;
        });
    }

//    public static Set<String> loadSpendUniqueAccounts(DappFeed dappFeed, String hash, int epochNo) {
//        return dappFeed.getSpendUniqueAccountsEpoch().computeIfAbsent(new EpochKey<>(epochNo, hash), hashEpochKey -> {
//            //log.warn("Unable to find unique addresses for hashEpochKey:{}", hashEpochKey);
//
//            return Set.of();
//        });
//    }

    public static long loadTokensBalance(DappFeed dappFeed, String assetId, int epochNo) {
        return dappFeed.getTokenHoldersBalanceEpoch().computeIfAbsent(new EpochKey<>(epochNo, assetId), assetIdEpochKey -> {
            log.warn("Unable to load balance for assetIdEpochKey:{}", assetIdEpochKey);
            return 0L;
        });
    }

    public static long loadSpendVolume(DappFeed dappFeed, String hash, int epochNo) {
        return dappFeed.getSpendVolumeEpoch().computeIfAbsent(new EpochKey<>(epochNo, hash), hashEpochKey -> {
            log.warn("Unable to load volume for hashEpochKey:{}", hashEpochKey);
            return 0L;
        });
    }

    public static long loadSpendTrxFee(DappFeed dappFeed, String hash, int epochNo) {
        return dappFeed.getSpendTrxFeesEpoch().computeIfAbsent(new EpochKey<>(epochNo, hash), hashEpochKey -> {
            log.warn("Unable to load fee for hashEpochKey:{}", hashEpochKey);
            return 0L;
        });
    }

    public static long loadTrxSize(DappFeed dappFeed, String hash, int epochNo) {
        return dappFeed.getSpendTrxSizesEpoch().computeIfAbsent(new EpochKey<>(epochNo, hash), hashEpochKey -> {
            log.warn("Unable to load trx size for hashEpochKey:{}", hashEpochKey);
            return 0L;
        });
    }

}
