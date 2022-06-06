package crfa.app.client.ada_price;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.*;

import java.util.Map;
import java.util.Optional;

@Builder
@Getter
@ToString
@Setter
@AllArgsConstructor
@JsonIgnoreProperties
@NoArgsConstructor
public class AdaPrices {

    private Map<String, Float> cardano;

    public Optional<Float> getUSD() {
        return Optional.ofNullable(cardano.get("usd"));
    }

    public Optional<Float> getEUR() {
        return Optional.ofNullable(cardano.get("eur"));
    }

}
