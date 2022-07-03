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

    long totalSmartContractsTransactionCount;

    long totalScriptsLocked;

    long totalScriptInvocationsCount;

    long totalDappsCount; // regardless if this is mint or spend; all dapp types

    Map<DAppType, Long> countDappsByDappType;

    BigDecimal adaPriceUSD;

    BigDecimal adaPriceEUR;

}
