package crfa.app.client.crfa_db_sync_api;

import io.micronaut.http.annotation.Get;
import io.micronaut.http.annotation.Header;
import io.micronaut.http.client.annotation.Client;
import org.reactivestreams.Publisher;

import java.util.Map;

import static io.micronaut.http.HttpHeaders.ACCEPT;
import static io.micronaut.http.HttpHeaders.USER_AGENT;

@Client("http://api.cardano.fans:8081")
@Header(name = USER_AGENT, value = "Micronaut HTTP Client")
@Header(name = ACCEPT, value = "application/json")
public interface CRFADbSyncApi {

    @Get("/top_scripts/{count}")
    Publisher<Map<String, Long>> topScripts(int count);

}
