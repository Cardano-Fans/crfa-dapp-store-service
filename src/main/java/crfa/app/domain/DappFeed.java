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

    Map<String, Long> balance;

    Map<String, Long> spendTransactionsCount;
    Map<String, Long> mintTransactionsCount;

    Map<String, Long> spendVolume;
    Map<String, Long> spendTrxFees;
    Map<String, Long> spendTrxSizes;

    // script type = SPEND on epoch level
    Map<String, Long> tokenHoldersBalance;
    Map<String, Set<String>> tokenHoldersAddresses;
    Map<String, Set<String>> spendUniqueAccounts;

    Map<EpochKey<String>, Long> balanceEpoch;
    Map<EpochKey<String>, Long> spendTransactionCountEpoch;
    Map<EpochKey<String>, Long> mintTransactionsCountEpoch;
    Map<EpochKey<String>, Long> spendVolumeEpoch;
    Map<EpochKey<String>, Long> spendTrxFeesEpoch;
    Map<EpochKey<String>, Long> spendTrxSizesEpoch;

    Map<EpochKey<String>, Long> tokenHoldersBalanceEpoch;
    Map<EpochKey<String>, Set<String>> tokenHoldersAddressesEpoch;
    Map<EpochKey<String>, Set<String>> spendUniqueAccountsEpoch;

}
