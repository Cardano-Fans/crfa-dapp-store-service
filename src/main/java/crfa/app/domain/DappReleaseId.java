package crfa.app.domain;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.*;

@Builder
@Getter
@ToString
@Setter
@AllArgsConstructor
@JsonIgnoreProperties
@NoArgsConstructor
@EqualsAndHashCode
public class DappReleaseId {

    String dappId;

    int releaseNumber;

    String hash;

}
