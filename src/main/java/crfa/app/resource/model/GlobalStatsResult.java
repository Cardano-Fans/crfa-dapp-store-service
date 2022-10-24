package crfa.app.resource.model;

import lombok.*;

import javax.annotation.Nullable;
import java.math.BigDecimal;

@Builder
@Getter
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class GlobalStatsResult {

    @Deprecated
    Long totalScriptsLocked;

    Long balance;

    // unified for transactionsCount and scriptInvocationsCount
    Long trxCount;

    Long volume;

    Long trxFees;

    @Nullable Double avgTrxFee;

    @Nullable Double avgTrxSize;

    long totalDappsCount; // regardless if this is mint or spend; all dapp types

    long totalUniqueAccounts;

    BigDecimal adaPriceUSD;

    BigDecimal adaPriceEUR;

}
