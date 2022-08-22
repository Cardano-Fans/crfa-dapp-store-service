package crfa.app.service;

import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RedissonClient;

import java.util.*;

@Singleton
@Slf4j
public class ScrollsOnChainDataService {

    @Inject
    private RedissonClient redissonClient;

    public Map<String, Long> mintScriptsCount(Collection<String> mintPolicyIds) {
        log.info("loading mint policy ids counts...");

        var m = new HashMap<String, Long>();

        mintPolicyIds.forEach(key -> {
            log.debug("Loading trx count for mintPolicyId:{}", key);

            var c = redissonClient.getAtomicLong("c4." + key).get();

            log.debug("Trx count for addr:{}, mintPolicyId:{}", key, c);

            m.put(key, c);
        });

        return m;
    }

    public Map<String, Long> scriptHashesCount(Collection<String> scriptHashes, boolean appendPrefix) {
        log.info("loading scriptHashes counts...");

        var m = new HashMap<String, Long>();

        scriptHashes.forEach(key -> {
            log.debug("Loading trx count for scriptHash:{}", key);

            var firstPrefix = appendPrefix ? ".71" : "";

            var c1 = redissonClient.getAtomicLong("c3" + firstPrefix + key).get();

            m.put(key, 0L);

            if (c1 > 0) {
                log.debug("Trx(c1) count for addr:{}, scriptHash:{}", key, c1);
                m.put(key, c1);
            } else {
                var secondPrefix = appendPrefix ? ".11" : "";
                var c2 = redissonClient.getAtomicLong("c3" + secondPrefix + key).get();

                log.debug("Trx(c2) count for addr:{}, scriptHash:{}", key, c2);
                if (c2 > 0) {
                    m.put(key, c2);
                } else {
                    var thirdPrefix = appendPrefix ? ".31" : "";
                    var c3 = redissonClient.getAtomicLong("c3" + thirdPrefix + key).get();

                    if (c3 > 0) {
                        log.debug("c3-31 hash:{} count:{}", key, c3);
                    }

                    m.put(key, c3);
                }
            }
        });

        return m;
    }

    public Map<String, Long> transactionsCount(Collection<String> addresses) {
        var transactionCountPerAddr = new HashMap<String, Long>();

        addresses.forEach(addr -> {
            log.debug("Loading trx count for addr:{}", addr);

            var c = redissonClient.getAtomicLong("c2." + addr).get();

            log.debug("Trx count for addr:{}, trxCount:{}", addr, c);

            transactionCountPerAddr.put(addr, c);
        });

        return transactionCountPerAddr;
    }

    public Map<String, Long> scriptLocked(Collection<String> addresses) {
        var lockedPerAddress = new HashMap<String, Long>();

        addresses.forEach(addr -> {
            log.debug("Loading script locked addr:{}", addr);

            var r = redissonClient.getAtomicLong("c1." + addr);

            if (r.isExists()) {
                var result= redissonClient.getAtomicLong("c1." + addr).get();
                log.debug("Script locked for addr:{}, lockedAda:{}", addr, result);

                if (result > 0) {
                    result = result / 1_000_000;
                }

                log.debug("Script locked for addr:{}, lockedAda:{}", addr, result);

                lockedPerAddress.put(addr, result);
            }
        });

        return lockedPerAddress;
    }

    public Set<String> listScriptHashes() {
        final var scriptHashesIterable = redissonClient.getKeys().getKeysByPattern("c3.*");

        var result = new HashSet<String>();
        for (var hash : scriptHashesIterable) {
            final var curratedHash = hash.replaceAll("c3.71", "").
                    replaceAll("c3.11", "")
                    .replaceAll("c3.31", "");

            result.add(curratedHash);
        }

        return result;
    }

    public Set<String> listMintHashes() {
        final var mintPolicyIdsIteable = redissonClient.getKeys().getKeysByPattern("c4.*");

        var result = new HashSet<String>();
        for (var hash : mintPolicyIdsIteable) {
            result.add(hash.replace("c4.", ""));
        }

        return result;
    }

}
