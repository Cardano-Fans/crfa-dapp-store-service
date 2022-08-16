package crfa.app.client.metadata;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import crfa.app.domain.Purpose;
import crfa.app.utils.MoreHex;
import io.micronaut.core.annotation.Nullable;
import lombok.*;

import java.util.Optional;

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

    @Deprecated
    boolean hasContract;

    @Deprecated
    boolean hasAudit;

    @Nullable
    @Deprecated
    Audit audit;

    @Nullable
    @Deprecated
    Contract contract;

    @Nullable
    String includeScriptBalanceFromAsset;

    public Optional<String> getAssetNameAsHex() {
        return Optional.ofNullable(includeScriptBalanceFromAsset)
                .map(asset -> String.format("%s%s", mintPolicyID, MoreHex.toHex(asset)));
    }

}
