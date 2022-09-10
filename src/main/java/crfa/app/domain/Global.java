package crfa.app.domain;

import lombok.*;

import java.math.BigDecimal;
import java.util.Map;

@Builder
@Getter
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class Global {

    long totalScriptsLocked;

    @Deprecated
    long totalScriptInvocationsCount;

    @Deprecated
    long totalSmartContractsTransactionCount;

    // unified for transactionsCount and scriptInvocationsCount
    Long trxCount;

    long totalDappsCount; // regardless if this is mint or spend; all dapp types

    Map<DAppType, Long> countDappsByDappType;

    BigDecimal adaPriceUSD;

    BigDecimal adaPriceEUR;

}
