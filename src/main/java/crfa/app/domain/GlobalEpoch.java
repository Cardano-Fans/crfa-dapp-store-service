package crfa.app.domain;

import lombok.*;

@Builder
@Getter
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class GlobalEpoch {

    Long inflowsOutflows;

    // unified for transactionsCount and scriptInvocationsCount
    Long trxCount;

    Long volume;

    Long fees;

    long totalUniqueAccounts;

}
