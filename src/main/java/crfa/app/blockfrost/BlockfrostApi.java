package crfa.app.blockfrost;

import io.blockfrost.sdk.api.model.PoolMetadata;
import io.micronaut.http.annotation.Get;
import io.micronaut.http.annotation.Header;
import io.micronaut.http.annotation.PathVariable;
import io.micronaut.http.annotation.QueryValue;
import io.micronaut.http.client.annotation.Client;

import java.util.Optional;
import java.util.Set;

import static io.micronaut.http.HttpHeaders.USER_AGENT;

@Client("https://cardano-mainnet.blockfrost.io/api/v0")
@Header(name = USER_AGENT, value = "Micronaut HTTP Client")
public interface BlockfrostApi {

    @Get("pools/{pool_id}/metadata")
    Optional<PoolMetadata> poolById(@Header("project_id") String projectId, @PathVariable("pool_id") String poolId);

    @Get("pools")
    Set<String> poolIds(@Header("project_id") String projectId, @QueryValue("count") int count, @QueryValue("page") int page);

}

