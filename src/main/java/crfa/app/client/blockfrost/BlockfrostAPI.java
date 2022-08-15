package crfa.app.client.blockfrost;

import io.blockfrost.sdk.api.AssetService;
import io.blockfrost.sdk.api.exception.APIException;
import io.blockfrost.sdk.api.model.AssetAddress;
import io.blockfrost.sdk.impl.AssetServiceImpl;
import io.micronaut.context.annotation.Value;
import io.micronaut.runtime.event.ApplicationStartupEvent;
import io.micronaut.runtime.event.annotation.EventListener;
import jakarta.inject.Singleton;
import lombok.extern.slf4j.Slf4j;

import java.util.Set;
import java.util.stream.Collectors;

import static io.blockfrost.sdk.api.util.Constants.BLOCKFROST_MAINNET_URL;

@Singleton
@Slf4j
public class BlockfrostAPI {

    private AssetService assetService;

    @Value("${blockfrost-projectId:}")
    private String blockfrostProjectId;

    public Set<String> tokenHolders(String policyId) throws APIException {
        if (policyId.equalsIgnoreCase("026a18d04a0c642759bb3d83b12e3344894e5c1c7b2aeb1a2113a570")) {
            return assetService.getAllAssetAddresses("026a18d04a0c642759bb3d83b12e3344894e5c1c7b2aeb1a2113a5704c")
                    .stream().map(AssetAddress::getAddress)
                    .collect(Collectors.toSet());
        }

        return Set.of();
    }

    @EventListener
    public void onStart(final ApplicationStartupEvent event) {
        log.info("Starting...");
        this.assetService = new AssetServiceImpl(BLOCKFROST_MAINNET_URL, blockfrostProjectId);
    }

 }
