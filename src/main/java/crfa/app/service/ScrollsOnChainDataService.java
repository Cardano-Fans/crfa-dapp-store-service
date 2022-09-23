package crfa.app.service;

import com.google.common.collect.Maps;
import crfa.app.domain.EpochKey;
import crfa.app.domain.Eras;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.commons.lang3.NotImplementedException;
import org.redisson.api.RedissonClient;
import org.redisson.client.codec.StringCodec;

import java.util.*;
import java.util.stream.Collectors;

import static crfa.app.domain.EraName.ALONZO;
import static crfa.app.domain.EraName.MARY;
import static java.lang.Integer.MAX_VALUE;
import static java.lang.String.format;

@Singleton
@Slf4j
public class ScrollsOnChainDataService {

    public static final int ONE_MLN = 1_000_000;

    @Inject
    private RedissonClient redissonClient;

    public Map<String, Long> mintScriptsCount(Collection<String> mintPolicyIds) {
        log.info("loading mint policy ids counts...");

        val m = new HashMap<String, Long>();

        val collection = "c4";

        mintPolicyIds.forEach(key -> {
            log.debug("Loading trx count for mintPolicyId:{}", key);

            val c = redissonClient.getAtomicLong(collection + "." + key).get();

            log.debug("Trx count for addr:{}, mintPolicyId:{}", key, c);

            m.put(key, c);
        });

        return m;
    }

    public Map<EpochKey<String>, Long> mintScriptsCountWithEpochs(Collection<String> mintPolicyIds) {
        log.info("loading mint policy ids counts...");

        val m = new HashMap<EpochKey<String>, Long>();

        val collection = "c8";

        val maybeCurrentEpochNo = currentEpoch();
        if (maybeCurrentEpochNo.isEmpty()) {
            log.warn("empty maybeCurrentEpochNo, returning empty scripts locked map.");
            return Maps.newHashMap();
        }
        val currentEpochNo = maybeCurrentEpochNo.get();

        val epochs = Eras.epochsBetween(MARY, currentEpochNo);

        for(int epochNo : epochs) {
            log.debug("mintScriptsCountWithEpochs - processing epochNo:{}", epochNo);
            mintPolicyIds.forEach(key -> {
                log.debug("Loading trx count for mintPolicyId:{}", key);

                val c = redissonClient.getAtomicLong(collection + "." + key).get();

                log.debug("Trx count for addr:{}, mintPolicyId:{}", key, c);

                m.put(new EpochKey<>(epochNo, key), c);
            });
        }

        return m;
    }

    public Map<String, Long> scriptHashesCount(Collection<String> scriptHashes,
                                               boolean appendPrefix) {
        log.info("loading scriptHashes counts...");

        val m = new HashMap<String, Long>();

        val collection = "c3";

        scriptHashes.forEach(key -> {
            log.debug("Loading trx count for scriptHash:{}", key);

            val firstPrefix = appendPrefix ? ".71" : "";

            val c1 = redissonClient.getAtomicLong(collection + firstPrefix + key).get();

            m.put(key, 0L);

            if (c1 > 0) {
                log.debug("Trx(c1) count for addr:{}, scriptHash:{}", key, c1);
                m.put(key, c1);
            } else {
                val secondPrefix = appendPrefix ? ".11" : "";
                val c2 = redissonClient.getAtomicLong(collection + secondPrefix + key).get();

                log.debug("Trx(c2) count for addr:{}, scriptHash:{}", key, c2);
                if (c2 > 0) {
                    m.put(key, c2);
                } else {
                    val thirdPrefix = appendPrefix ? ".31" : "";
                    val c3 = redissonClient.getAtomicLong(collection + thirdPrefix + key).get();

                    if (c3 > 0) {
                        log.debug("c3-31 hash:{} count:{}", key, c3);
                    }

                    m.put(key, c3);
                }
            }
        });

        return m;
    }

    public Map<EpochKey<String>, Long> scriptHashesCountWithEpochs(Collection<String> scriptHashes,
                                                                   boolean appendPrefix) {
        log.info("loading scriptHashes counts...");

        val m = new HashMap<EpochKey<String>, Long>();

        val collection = "c7";

        val maybeCurrentEpochNo = currentEpoch();
        if (maybeCurrentEpochNo.isEmpty()) {
            log.warn("empty maybeCurrentEpochNo, returning empty scripts locked map.");
            return Maps.newHashMap();
        }
        val currentEpochNo = maybeCurrentEpochNo.get();

        val epochs = Eras.epochsBetween(ALONZO, currentEpochNo);

        for(int epochNo : epochs) {
            log.debug("scriptHashesCountWithEpochs - processing epochNo:{}", epochNo);

            scriptHashes.forEach(k -> {
                val key = new EpochKey<>(epochNo, k);
                log.debug("Loading trx count for scriptHash:{}", key);

                val firstPrefix = appendPrefix ? ".71" : "";

                val c1 = redissonClient.getAtomicLong(collection + firstPrefix + key).get();

                m.put(key, 0L);

                if (c1 > 0) {
                    log.debug("Trx(c1) count for addr:{}, scriptHash:{}", key, c1);
                    m.put(key, c1);
                } else {
                    val secondPrefix = appendPrefix ? ".11" : "";
                    val c2 = redissonClient.getAtomicLong(collection + secondPrefix + key).get();

                    log.debug("Trx(c2) count for addr:{}, scriptHash:{}", key, c2);
                    if (c2 > 0) {
                        m.put(key, c2);
                    } else {
                        val thirdPrefix = appendPrefix ? ".31" : "";
                        val c3 = redissonClient.getAtomicLong(collection + thirdPrefix + key).get();

                        if (c3 > 0) {
                            log.debug("c3-31 hash:{} count:{}", key, c3);
                        }

                        m.put(key, c3);
                    }
                }
            });
        }

        return m;
    }

    public Map<String, Long> transactionsCount(Collection<String> addresses) {
        val transactionCountPerAddr = new HashMap<String, Long>();

        val collection = "c2";

        addresses.forEach(addr -> {
            log.debug("Loading trx count for addr:{}", addr);

            val result = redissonClient.getAtomicLong(collection + "." + addr).get();

            log.debug("Trx count for addr:{}, trxCount:{}", addr, result);

            transactionCountPerAddr.put(addr, result);
        });

        return transactionCountPerAddr;
    }

    public Map<EpochKey<String>, Long> transactionsCountWithEpochs(Collection<String> addresses) {
        val transactionCountPerAddr = new HashMap<EpochKey<String>, Long>();

        val collection = "c6";

        val maybeCurrentEpochNo = currentEpoch();
        if (maybeCurrentEpochNo.isEmpty()) {
            log.warn("empty maybeCurrentEpochNo, returning empty scripts locked map.");
            return Maps.newHashMap();
        }
        val currentEpochNo = maybeCurrentEpochNo.get();

        val epochs = Eras.epochsBetween(ALONZO, currentEpochNo);

        for(int epochNo : epochs) {
            log.debug("transactionsCountWithEpochs - processing epochNo:{}", epochNo);

            addresses.forEach(addr -> {
                log.debug("Loading trx count for addr:{}", addr);

                val result = redissonClient.getAtomicLong(collection + "." + addr).get();

                log.debug("Trx count for addr:{}, trxCount:{}", addr, result);

                transactionCountPerAddr.put(new EpochKey<>(epochNo, addr), result);
            });
        }

        return transactionCountPerAddr;
    }
    public Map<String, Long> scriptLocked(Collection<String> addresses) {
        val lockedPerAddress = new HashMap<String, Long>();

        val collection = "c1";

        addresses.forEach(addr -> {
            log.debug("Loading script locked addr:{}", addr);

            val r = redissonClient.getAtomicLong(collection + "." + addr);

            if (r.isExists()) {
                val result = r.get();

                log.debug("Script locked for addr:{}, lockedAda:{}", addr, result);

                val resultADA = result / ONE_MLN;
                log.debug("Script locked for addr:{}, lockedAda:{}", addr, resultADA);

                lockedPerAddress.put(addr, resultADA);
            } else {
                lockedPerAddress.put(addr, 0L);
            }
        });

        return lockedPerAddress;
    }

    public Map<String, Long> scriptLockedAtEpoch(Collection<String> addresses, int epochNo) {
        throw new NotImplementedException();
    }

    public Map<EpochKey<String>, Long> scriptLockedWithEpochs(Collection<String> addresses) {
        val lockedPerAddress = new HashMap<EpochKey<String>, Long>();

        val collection = "c5";

        val maybeCurrentEpochNo = currentEpoch();
        if (maybeCurrentEpochNo.isEmpty()) {
            log.warn("empty maybeCurrentEpochNo, returning empty scripts locked map.");
            return Maps.newHashMap();
        }
        val currentEpochNo = maybeCurrentEpochNo.get();

        val epochs = Eras.epochsBetween(MARY, currentEpochNo);

        for(int epochNo : epochs) {
            log.debug("scriptLockedWithEpochs - processing epochNo:{}", epochNo);

            addresses.forEach(addr -> {
                log.debug("Loading script locked addr:{} for maybeCurrentEpochNo:{}", addr, epochNo);

                val key = format(collection + ".%s.%d", addr, epochNo);

                val r = redissonClient.getAtomicLong(key);

                if (r.isExists()) {
                    val result = r.get();

                    val resultAda = result / ONE_MLN;
                    log.debug("Script locked for addr:{}, lockedAda:{}, epoch:{}", addr, resultAda, epochNo);

                    lockedPerAddress.put(new EpochKey<>(epochNo, addr), resultAda);
                } else {
                    lockedPerAddress.put(new EpochKey<>(epochNo, addr), 0L);
                }
            });
        }

        return lockedPerAddress;
    }

    public Optional<Integer> currentEpoch() {
        val r = redissonClient.getAtomicLong("c13.epoch_no");

        if (r.isExists()) {
            return Optional.of((int) r.get());
        }

        return Optional.empty();
    }

    // current asset holders, that is per this epoch

    // ZRANGEBYSCORE "c14.026a18d04a0c642759bb3d83b12e3344894e5c1c7b2aeb1a2113a5704c" 1 +inf
    public Set<String> getCurrentAssetHolders(String assetId) {
        val assetMembersRScored = redissonClient.<String>getScoredSortedSet(format("%s.%s", "c14", assetId), new StringCodec());
        val tokenHolders= assetMembersRScored.valueRangeReversed(1, MAX_VALUE);

        return Set.copyOf(tokenHolders);
    }

    // TODO allow to specify from which epoch / era to load data
    // ZRANGEBYSCORE "c14.026a18d04a0c642759bb3d83b12e3344894e5c1c7b2aeb1a2113a5704c".364 1 +inf
    public Map<Integer, Set<String>> getAssetHoldersWithEpochs(String assetId) {
        // c15
        val currentEpoch = currentEpoch().orElseThrow(() -> new RuntimeException("unable to read current epoch"));

        val epochs = Eras.epochsBetween(MARY, currentEpoch);

        val tokenHoldersPerEpoch = new HashMap<Integer, Set<String>>();

        for (int epoch : epochs) {
            val assetMembersRScored = redissonClient.<String>getScoredSortedSet(format("%s.%s.%d", "c15", assetId, epoch), new StringCodec());

            val assetMembersPerEpoch = assetMembersRScored.stream().collect(Collectors.toSet());

            tokenHoldersPerEpoch.put(epoch, assetMembersPerEpoch);
        }

        return tokenHoldersPerEpoch;
    }

//    public Set<String> listScriptHashes() {
//        val collection = "c3";
//
//        val scriptHashesIterable = redissonClient.getKeys().getKeysByPattern(collection + ".*");
//
//        val result = new HashSet<String>();
//        for (val hash : scriptHashesIterable) {
//            val curratedHash = hash.replaceAll(collection + ".71", "").
//                    replaceAll(collection + ".11", "")
//                    .replaceAll(collection + ".31", "");
//
//            result.add(curratedHash);
//        }
//
//        return result;
//    }
//
//    public Set<String> listMintHashes() {
//        val collection = "c4";
//
//        val mintPolicyIdsIteable = redissonClient.getKeys().getKeysByPattern(collection + ".*");
//
//        val result = new HashSet<String>();
//        for (val hash : mintPolicyIdsIteable) {
//            result.add(hash.replace(collection + ".", ""));
//        }
//
//        return result;
//    }

}
