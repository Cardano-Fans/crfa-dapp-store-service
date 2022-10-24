package crfa.app.resource.model;

import lombok.*;

import javax.annotation.Nullable;

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

    @Nullable Double avgTrxFee;

    @Nullable Double avgTrxSize;

    long totalUniqueAccounts;

}
