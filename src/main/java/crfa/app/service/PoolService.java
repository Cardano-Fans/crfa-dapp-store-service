package crfa.app.service;


import crfa.app.blockfrost.BlockfrostApi;
import crfa.app.domain.Pool;
import crfa.app.repository.PoolRepository;
import io.micronaut.context.annotation.Value;
import io.micronaut.runtime.event.annotation.EventListener;
import io.micronaut.runtime.server.event.ServerStartupEvent;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import lombok.extern.slf4j.Slf4j;
import lombok.val;

import java.util.HashSet;
import java.util.Set;

import static java.lang.Integer.MAX_VALUE;

@Singleton
@Slf4j
public class PoolService {

    @Inject
    private BlockfrostApi blockfrostAPI;

    @Inject
    private PoolRepository poolRepository;

    @Value("${blockfrost-projectId}")
    private String blockfrostProjectId;

    public Set<String> getAllPoolIds() {
        val ids = new HashSet<String>();

        for (int page = 0; page < MAX_VALUE; page ++) {
            try {
                val poolIds = blockfrostAPI.poolIds(blockfrostProjectId, 100, page);
                log.info("Fetched pools:{}", poolIds.size());
                if (poolIds.size() == 0) {
                    return ids;
                }
                ids.addAll(poolIds);
            } catch (Exception e) {
                throw new RuntimeException("blockfrost error", e);
            }
        }

        return ids;
    }

    public void updatePools() {
        log.info("pool update scheduled...");
        val poolIds = getAllPoolIds();
        if (poolIds.isEmpty()) {
            log.warn("pool ids are empty, ignoring...");
            return;
        }

        log.info("updating pools info based on pool ids...");

        poolIds.forEach(poolId -> {
            blockfrostAPI.poolById(blockfrostProjectId, poolId).ifPresent(poolMetadata -> {
                if (poolMetadata.getPoolId() == null) {
                    log.warn("pool id is null?, pool:{}", poolMetadata);
                } else {
                    poolRepository.upsert(Pool.builder()
                            .hex(poolMetadata.getHex())
                            .bech32(poolMetadata.getPoolId())
                            .ticker(poolMetadata.getTicker())
                            .build());
                }
            });
        });
        log.info("updating pools info update complete.");
    }

    @EventListener
    public void onStartup(ServerStartupEvent event) {
        //updatePools();
    }
}
