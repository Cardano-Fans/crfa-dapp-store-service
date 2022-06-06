package crfa.app.service;

import crfa.app.client.crfa_db_sync_api.CRFADbSyncApi;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Mono;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Singleton
@Slf4j
public class DbSyncService {

    @Inject
    private CRFADbSyncApi crfaDbSyncApi;

    public Map<String, Long> topScripts(int count) {
        return Mono.from(crfaDbSyncApi.topScripts(count)).block();
        //return Map.of();
    }

    public Map<String, Long>  transactionsCount(List<String> addresses) {
        var transactionCountPerAddr = new HashMap<String, Long>();

        addresses.forEach(addr -> {
            log.info("Loading trx count for addr:{}", addr);

            var result= Mono.from(crfaDbSyncApi.transactionsCount(addr)).block();
            var trxCount = result.getOrDefault("transactionsCount", 0L);
            log.info("Trx count for addr:{}, trxCount:{}", addr, trxCount);

            transactionCountPerAddr.put(addr, trxCount);
        });

        return transactionCountPerAddr;
    }

    public Map<String, Long> scriptLocked(List<String> addresses) {
        var lockedPerAddress = new HashMap<String, Long>();

        addresses.forEach(addr -> {
            log.info("Loading script locked addr:{}", addr);

            var result= Mono.from(crfaDbSyncApi.scriptLocked(addr)).block();
            var lockedAda= result.getOrDefault(addr, 0L);
            log.info("Script locked for addr:{}, lockedAda:{}", addr, lockedAda);

            lockedPerAddress.put(addr, lockedAda);
        });

        return lockedPerAddress;
    }

}
