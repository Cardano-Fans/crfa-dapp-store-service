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
@JsonIgnoreProperties(ignoreUnknown = true)
@NoArgsConstructor
public class DappReleaseItem {

    String releaseName;

    float releaseNumber;

    @Nullable
    Audit audit;

    @Nullable
    Contract contract;

    List<ScriptItem> scripts;

}
