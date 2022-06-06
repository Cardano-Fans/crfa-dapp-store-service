package crfa.app.client.metadata;

import io.micronaut.http.annotation.Get;
import io.micronaut.http.annotation.Header;
import io.micronaut.http.client.annotation.Client;
import org.reactivestreams.Publisher;

import java.util.List;

import static io.micronaut.http.HttpHeaders.ACCEPT;
import static io.micronaut.http.HttpHeaders.USER_AGENT;

@Client("http://api.cardano.fans:8080")
@Header(name = USER_AGENT, value = "Micronaut HTTP Client")
@Header(name = ACCEPT, value = "application/json")
public interface CRFAMetaDataServiceClient {

    @Get("/metadata/dapps/list")
    Publisher<List<DappSearchItem>> fetchAllDapps();

}
