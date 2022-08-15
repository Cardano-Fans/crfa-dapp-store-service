package crfa.app.service;

import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RedissonClient;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

@Singleton
@Slf4j
public class ScrollsService {

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

    public Map<String, Long> scriptHashesCount(Collection<String> scriptHashes) {
        log.info("loading scriptHashes counts...");

        var m = new HashMap<String, Long>();

        scriptHashes.forEach(key -> {
            log.debug("Loading trx count for scriptHash:{}", key);

            var c1 = redissonClient.getAtomicLong("c3.71" + key).get();

            m.put(key, 0L);

            if (c1 > 0) {
                log.debug("Trx(c1) count for addr:{}, scriptHash:{}", key, c1);
                m.put(key, c1);
            } else {
                var c2 = redissonClient.getAtomicLong("c3.11" + key).get();

                log.debug("Trx(c2) count for addr:{}, scriptHash:{}", key, c2);

                m.put(key, c2);
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

}
