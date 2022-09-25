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
    Map<String, Long> volumePerContractAddress;
    Map<String, Long> transactionCountsPerContractAddress;
    Map<String, Long> tokenHoldersBalance;

    Map<EpochKey<String>, Long> invocationsCountPerHashEpoch;
    Map<EpochKey<String>, Long> scriptLockedPerContractAddressEpoch;
    Map<EpochKey<String>, Long> transactionCountsPerContractAddressEpoch;
    Map<EpochKey<String>, Long> tokenHoldersBalanceEpoch;
    Map<EpochKey<String>, Long> volumePerContractAddressEpoch;

}
