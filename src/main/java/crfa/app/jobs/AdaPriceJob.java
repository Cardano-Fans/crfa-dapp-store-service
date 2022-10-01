package crfa.app.jobs;


import crfa.app.client.ada_price.AdaPriceClient;
import crfa.app.domain.AdaPricePerDay;
import crfa.app.repository.total.AdaPriceRepository;
import io.micronaut.scheduling.annotation.Scheduled;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import reactor.core.publisher.Mono;

import java.text.SimpleDateFormat;
import java.util.Date;

@Singleton
@Slf4j
public class AdaPriceJob {

    @Inject
    private AdaPriceClient adaPriceClient;

    @Inject
    private AdaPriceRepository adaPriceRepository;

    @Scheduled(fixedDelay = "15m", initialDelay = "5s")
    public void onScheduled() {
        log.info("Ada price update...");

        val format = new SimpleDateFormat("yyyy-MM-dd");

        val adaPrices = Mono.from(adaPriceClient.adaPrices()).block();

        long now = System.currentTimeMillis();

        adaPrices.getUSD().ifPresent(priceUsd -> {
            adaPriceRepository.updatePrice(AdaPricePerDay.builder()
                    .key(String.format("%s.%s", format.format(new Date(now)), "USD"))
                    .price(priceUsd)
                    .currency("USD")
                    .modDate(new Date(now))
                    .build()
            );
        });

        adaPrices.getEUR().ifPresent(priceUsd -> {
            adaPriceRepository.updatePrice(AdaPricePerDay.builder()
                    .key(String.format("%s.%s", format.format(new Date(now)), "EUR"))
                    .price(priceUsd)
                    .currency("EUR")
                    .modDate(new Date(now))
                    .build()
            );
        });
        log.info("Ada price update completed.");

    }

 }
