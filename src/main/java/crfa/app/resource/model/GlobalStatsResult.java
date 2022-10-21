package crfa.app.resource.model;

import lombok.*;

import java.math.BigDecimal;

@Builder
@Getter
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class GlobalStatsResult {

    Long totalScriptsLocked;

    // unified for transactionsCount and scriptInvocationsCount
    Long trxCount;

    Long volume;

    Long fees;

    Double avgFee;

    long totalDappsCount; // regardless if this is mint or spend; all dapp types

    long totalUniqueAccounts;

    BigDecimal adaPriceUSD;

    BigDecimal adaPriceEUR;

}
