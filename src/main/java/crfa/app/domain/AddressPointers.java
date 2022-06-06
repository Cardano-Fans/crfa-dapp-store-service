package crfa.app.domain;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import io.micronaut.core.annotation.Nullable;
import lombok.*;

@Builder
@Getter
@Setter
@ToString
@AllArgsConstructor
@JsonIgnoreProperties
@NoArgsConstructor
public class AddressPointers {

    @Nullable
    String contractAddress;

    String scriptHash;

}
