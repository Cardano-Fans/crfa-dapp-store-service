package crfa.app.service.processor.epoch;

import crfa.app.domain.DappFeed;
import crfa.app.domain.EpochKey;
import lombok.extern.slf4j.Slf4j;

import java.util.Set;

@Slf4j
public class ProcessorHelper {

    public static long loadInvocations(DappFeed dappFeed, String hash, int epochNo) {
        return dappFeed.getInvocationsCountEpoch().computeIfAbsent(new EpochKey<>(epochNo, hash), hashEpochKey -> {
            log.warn("Unable to find total invocations for hashEpochKey:{}", hashEpochKey);

            return 0L;
        });
    }

    public static long loadAdaBalance(DappFeed dappFeed, String hash, int epochNo) {
        return dappFeed.getScriptLockedEpoch().computeIfAbsent(new EpochKey<>(epochNo, hash), hashEpochKey -> {
            log.warn("Unable to find scriptsLocked for hashEpochKey:{}", hashEpochKey);

            return 0L;
        });
    }

    public static Set<String> loadUniqueAccounts(DappFeed dappFeed, String hash, int epochNo) {
        return dappFeed.getUniqueAccountsEpoch().computeIfAbsent(new EpochKey<>(epochNo, hash), hashEpochKey -> {
            log.warn("Unable to find unique addresses for hashEpochKey:{}", hashEpochKey);

            return Set.of();
        });
    }

    public static long loadTokensBalance(DappFeed dappFeed, String assetId, int epochNo) {
        return dappFeed.getTokenHoldersBalanceEpoch().computeIfAbsent(new EpochKey<>(epochNo, assetId), assetIdEpochKey -> {
            log.warn("Unable to load balance for assetIdEpochKey:{}", assetIdEpochKey);
            return 0L;
        });
    }

    public static long loadVolume(DappFeed dappFeed, String hash, int epochNo) {
        return dappFeed.getVolumeEpoch().computeIfAbsent(new EpochKey<>(epochNo, hash), hashEpochKey -> {
            log.warn("Unable to load volume for hashEpochKey:{}", hashEpochKey);
            return 0L;
        });
    }

    public static long loadFee(DappFeed dappFeed, String hash, int epochNo) {
        return dappFeed.getFeesEpoch().computeIfAbsent(new EpochKey<>(epochNo, hash), hashEpochKey -> {
            log.warn("Unable to load fee for hashEpochKey:{}", hashEpochKey);
            return 0L;
        });
    }

    public static long loadTrxSize(DappFeed dappFeed, String hash, int epochNo) {
        return dappFeed.getTrxSizesEpoch().computeIfAbsent(new EpochKey<>(epochNo, hash), hashEpochKey -> {
            log.warn("Unable to load trx size for hashEpochKey:{}", hashEpochKey);
            return 0L;
        });
    }

}
