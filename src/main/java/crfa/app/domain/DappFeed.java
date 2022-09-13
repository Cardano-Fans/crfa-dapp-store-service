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
    Map<String, Long> tokenHoldersBalance;

    Map<EpochValue<String>, Long> invocationsCountPerHashEpoch;
    Map<EpochValue<String>, Long> scriptLockedPerContractAddressEpoch;
    Map<EpochValue<String>, Long> transactionCountsPerContractAddressEpoch;
    Map<EpochValue<String>, Long> tokenHoldersBalanceEpoch;

}
