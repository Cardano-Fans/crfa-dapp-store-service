package crfa.app.client.metadata;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
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
public class DappReleaseItem {

    String releaseName;

    float releaseNumber;

    List<ScriptItem> scripts;

    @Nullable
    Audit audit;

    @Nullable
    Contract contract;

}
