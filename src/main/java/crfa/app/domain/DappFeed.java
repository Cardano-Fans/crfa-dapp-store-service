package crfa.app.domain;

import crfa.app.client.metadata.DappSearchItem;
import lombok.*;

import java.util.List;
import java.util.Map;
import java.util.Set;

@Builder
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class DappFeed {

    List<DappSearchItem> dappSearchResult;

    Map<String, Long> invocationsCount;
    Map<String, Long> getAdaBalance;
    Map<String, Long> volume;
    Map<String, Long> tokenHoldersBalance;
    Map<String, Set<String>> tokenHoldersAddresses;
    Map<String, Set<String>> uniqueAccounts;

    Map<EpochKey<String>, Long> invocationsCountEpoch;
    Map<EpochKey<String>, Long> scriptLockedEpoch;
    Map<EpochKey<String>, Long> tokenHoldersBalanceEpoch;
    Map<EpochKey<String>, Long> volumeEpoch;
    Map<EpochKey<String>, Set<String>> tokenHoldersAddressesEpoch;
    Map<EpochKey<String>, Set<String>> uniqueAccountsEpoch;

}
