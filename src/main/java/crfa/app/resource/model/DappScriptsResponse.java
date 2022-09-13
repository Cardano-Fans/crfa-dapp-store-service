package crfa.app.resource.model;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Builder
@Getter
@AllArgsConstructor
@NoArgsConstructor
public class DappScriptsResponse {

    DappReleaseResult release;

    List<DAppReleaseItemResult> scripts;

}
