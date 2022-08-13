package crfa.app.client.metadata;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import crfa.app.domain.Purpose;
import io.micronaut.core.annotation.Nullable;
import lombok.*;

import java.util.List;

@Builder
@Getter
@ToString
@Setter
@AllArgsConstructor
@JsonIgnoreProperties
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
    List<String> tokenHolders;

}
