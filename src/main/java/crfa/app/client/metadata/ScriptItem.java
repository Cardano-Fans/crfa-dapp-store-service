package crfa.app.client.metadata;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import crfa.app.domain.Purpose;
import io.micronaut.core.annotation.Nullable;
import lombok.*;

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

    String scriptHash;

    String mintPolicyID;

    boolean hasContract;

    boolean hasAudit;

    @Nullable
    Audit audit;

    @Nullable
    Contract contract;

}
