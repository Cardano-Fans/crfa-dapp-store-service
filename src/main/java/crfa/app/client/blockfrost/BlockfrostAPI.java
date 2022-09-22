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

    public Set<String> tokenHolders(String assetHex) throws APIException {

        return assetService.getAllAssetAddresses(assetHex)
                .stream()
                .peek(a -> log.info("addr:{}, q:{}", a.getAddress(), a.getQuantity()))
                .map(AssetAddress::getAddress)
                .collect(Collectors.toSet());
    }

    @EventListener
    public void onStart(final ApplicationStartupEvent event) {
        log.info("Starting...");
        this.assetService = new AssetServiceImpl(BLOCKFROST_MAINNET_URL, blockfrostProjectId);
    }

 }
