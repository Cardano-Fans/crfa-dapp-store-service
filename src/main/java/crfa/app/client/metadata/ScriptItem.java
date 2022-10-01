package crfa.app.client.metadata;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import crfa.app.domain.Purpose;
import io.micronaut.core.annotation.Nullable;
import lombok.*;

import java.util.Optional;

import static crfa.app.utils.MoreHex.toHex;

@Builder
@Getter
@ToString
@Setter
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
@NoArgsConstructor
public class ScriptItem {

    String id;

    @Nullable
    String name;

    int version;

    Purpose purpose;

    String contractAddress;

    @Nullable
    String scriptHash;

    String mintPolicyID;

    @Nullable
    String includeScriptBalanceFromAsset;

    public Optional<String> getAssetId() {
        return Optional.ofNullable(includeScriptBalanceFromAsset)
                .map(asset -> String.format("%s%s", mintPolicyID, toHex(asset)));
    }

}
