package crfa.app.service;

import com.google.common.collect.ContiguousSet;
import com.google.common.collect.DiscreteDomain;
import com.google.common.collect.Maps;
import crfa.app.domain.EpochValue;
import crfa.app.domain.Era;
import crfa.app.domain.EraName;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.redisson.api.RedissonClient;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import static crfa.app.domain.EraName.ALONZO;

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

    public Map<EpochValue<String>, Long> mintScriptsCountWithEpochs(Collection<String> mintPolicyIds) {
        log.info("loading mint policy ids counts...");

        val m = new HashMap<EpochValue<String>, Long>();

        val collection = "c8";

        val maybeCurrentEpochNo = currentEpoch();
        if (maybeCurrentEpochNo.isEmpty()) {
            log.warn("empty maybeCurrentEpochNo, returning empty scripts locked map.");
            return Maps.newHashMap();
        }
        val currentEpochNo = maybeCurrentEpochNo.get();

        val epochs = new Era(EraName.MARY).allEpochNumbersBetween(currentEpochNo);

        for(int epochNo : ContiguousSet.create(epochs, DiscreteDomain.integers())) {
            log.info("mintScriptsCountWithEpochs - processing epochNo:{}", epochNo);
            mintPolicyIds.forEach(key -> {
                log.debug("Loading trx count for mintPolicyId:{}", key);

                val c = redissonClient.getAtomicLong(collection + "." + key).get();

                log.debug("Trx count for addr:{}, mintPolicyId:{}", key, c);

                m.put(new EpochValue<>(epochNo, key), c);
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

    public Map<EpochValue<String>, Long> scriptHashesCountWithEpochs(Collection<String> scriptHashes,
                                                                     boolean appendPrefix) {
        log.info("loading scriptHashes counts...");

        val m = new HashMap<EpochValue<String>, Long>();

        val collection = "c7";

        val maybeCurrentEpochNo = currentEpoch();
        if (maybeCurrentEpochNo.isEmpty()) {
            log.warn("empty maybeCurrentEpochNo, returning empty scripts locked map.");
            return Maps.newHashMap();
        }
        val currentEpochNo = maybeCurrentEpochNo.get();

        val epochs = new Era(ALONZO).allEpochNumbersBetween(currentEpochNo);

        for(int epochNo : ContiguousSet.create(epochs, DiscreteDomain.integers())) {
            log.info("scriptHashesCountWithEpochs - processing epochNo:{}", epochNo);

            scriptHashes.forEach(k -> {
                val key = new EpochValue<>(epochNo, k);
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

    public Map<EpochValue<String>, Long> transactionsCountWithEpochs(Collection<String> addresses) {
        val transactionCountPerAddr = new HashMap<EpochValue<String>, Long>();

        val collection = "c6";

        val maybeCurrentEpochNo = currentEpoch();
        if (maybeCurrentEpochNo.isEmpty()) {
            log.warn("empty maybeCurrentEpochNo, returning empty scripts locked map.");
            return Maps.newHashMap();
        }
        val currentEpochNo = maybeCurrentEpochNo.get();

        val epochs = new Era(ALONZO).allEpochNumbersBetween(currentEpochNo);

        for(int epochNo : ContiguousSet.create(epochs, DiscreteDomain.integers())) {
            log.info("transactionsCountWithEpochs - processing epochNo:{}", epochNo);

            addresses.forEach(addr -> {
                log.debug("Loading trx count for addr:{}", addr);

                val result = redissonClient.getAtomicLong(collection + "." + addr).get();

                log.debug("Trx count for addr:{}, trxCount:{}", addr, result);

                transactionCountPerAddr.put(new EpochValue<>(epochNo, addr), result);
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

                if (result > 0) {
                    val resultADA = result / ONE_MLN;
                    log.debug("Script locked for addr:{}, lockedAda:{}", addr, resultADA);

                    lockedPerAddress.put(addr, resultADA);
                } else {
                    lockedPerAddress.put(addr, 0L);
                }
            }
        });

        return lockedPerAddress;
    }

    public Map<EpochValue<String>, Long> scriptLockedWithEpochs(Collection<String> addresses) {
        val lockedPerAddress = new HashMap<EpochValue<String>, Long>();

        val collection = "c5";

        val maybeCurrentEpochNo = currentEpoch();
        if (maybeCurrentEpochNo.isEmpty()) {
            log.warn("empty maybeCurrentEpochNo, returning empty scripts locked map.");
            return Maps.newHashMap();
        }
        val currentEpochNo = maybeCurrentEpochNo.get();

        val epochs = new Era(ALONZO).allEpochNumbersBetween(currentEpochNo);

        for(int epochNo : ContiguousSet.create(epochs, DiscreteDomain.integers())) {
            log.info("scriptLockedWithEpochs - processing epochNo:{}", epochNo);

            addresses.forEach(addr -> {
                log.debug("Loading script locked addr:{} for maybeCurrentEpochNo:{}", addr, epochNo);

                val key = String.format(collection + ".%d.%s", epochNo, addr);

                val r = redissonClient.getAtomicLong(key);

                if (r.isExists()) {
                    val result = r.get();
                    log.debug("Script locked for addr:{}, lockedAda:{}", addr, result);

                    if (result > 0) {
                        val resultAda = result / ONE_MLN;
                        log.debug("Script locked for addr:{}, lockedAda:{}", addr, resultAda);

                        lockedPerAddress.put(new EpochValue<>(epochNo, addr), resultAda);
                    }
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
