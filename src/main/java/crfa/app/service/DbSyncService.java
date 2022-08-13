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

    @Deprecated
    public Map<String, Long> scriptLocked(List<String> addresses) {
        log.info("Loading script locked values for addresses count:{}", addresses.size());

        var lockedPerAddress = new HashMap<String, Long>();

        addresses.forEach(addr -> {
            log.debug("Loading script locked addr:{}", addr);

            var result= Mono.from(crfaDbSyncApi.scriptLocked(addr)).block();
            var lockedAda= result.getOrDefault(addr, 0L);
            log.debug("Script locked for addr:{}, lockedAda:{}", addr, lockedAda);

            lockedPerAddress.put(addr, lockedAda);
        });

        log.info("loaded script locked values for address count:{}", addresses.size());

        return lockedPerAddress;
    }

}
