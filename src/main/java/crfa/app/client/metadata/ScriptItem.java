package crfa.app.client.metadata;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import crfa.app.domain.Purpose;
import io.micronaut.core.annotation.Nullable;
import lombok.*;

import java.util.Optional;

import static crfa.app.domain.Purpose.SPEND;
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

    @Nullable
    String scriptHash;

    @Nullable
    String mintPolicyID;

    int plutusVersion;

    String fullScriptHash;

    @Nullable
    String includeScriptBalanceFromAsset;

    public String getUnifiedHash() {
        return purpose == SPEND ? fullScriptHash : mintPolicyID;
    }

    public Optional<String> getAssetId() {
        return Optional.ofNullable(includeScriptBalanceFromAsset)
                .map(asset -> String.format("%s.%s", mintPolicyID, toHex(asset)));
    }

}
