package crfa.app.domain;

import crfa.app.client.metadata.DappSearchItem;
import lombok.*;

import java.util.List;
import java.util.Map;

@Builder
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class DappFeed {

    List<DappSearchItem> dappSearchResult;
    Map<String, Long> invocationsCountPerHash;
    Map<String, Long> scriptLockedPerContractAddress;
    Map<String, Long> transactionCountsPerContractAddress;

    // for WingRinders we need a special case that a mintPolicyId is in fact a link to token holders addresses
    // here a key represents MintPolicyId and value as Long represents sum of ADA balance for all of token holders
    Map<String, Long> tokenHoldersBalance;

}
