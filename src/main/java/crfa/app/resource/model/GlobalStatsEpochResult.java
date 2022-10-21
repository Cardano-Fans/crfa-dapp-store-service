package crfa.app.resource.model;

import lombok.*;

@Builder
@Getter
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class GlobalStatsEpochResult {

    Long inflowsOutflows;

    // unified for transactionsCount and scriptInvocationsCount
    Long trxCount;

    Long volume;

    Long fees;

    Double avgFee;

    long totalUniqueAccounts;

}
