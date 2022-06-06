package crfa.app.client.ada_price;

import io.micronaut.http.annotation.Get;
import io.micronaut.http.annotation.Header;
import io.micronaut.http.client.annotation.Client;
import org.reactivestreams.Publisher;

import static io.micronaut.http.HttpHeaders.ACCEPT;
import static io.micronaut.http.HttpHeaders.USER_AGENT;

@Client("https://api.coingecko.com")
@Header(name = USER_AGENT, value = "Micronaut HTTP Client")
@Header(name = ACCEPT, value = "application/json")
public interface AdaPriceClient {

    @Get("/api/v3/simple/price?ids=cardano&vs_currencies=usd,eur")
    Publisher<AdaPrices> adaPrices();

}
