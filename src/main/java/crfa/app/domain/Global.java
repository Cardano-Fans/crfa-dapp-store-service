package crfa.app.domain;

import lombok.*;

import java.math.BigDecimal;

@Builder
@Getter
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class Global {

    Long totalScriptsLocked;

    // unified for transactionsCount and scriptInvocationsCount
    Long trxCount;

    Long volume;

    long totalDappsCount; // regardless if this is mint or spend; all dapp types

    long totalUniqueAccounts;

    BigDecimal adaPriceUSD;

    BigDecimal adaPriceEUR;

}
