package crfa.app.domain;

import lombok.*;

import java.math.BigDecimal;

@Builder
@Getter
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class Global {


    long totalTransactionsCount;

    long totalSmartContractsTransactionCount;

    long totalTVL;

    long totalScriptInvocationsCount;

    BigDecimal adaPriceUSD;

    BigDecimal adaPriceEUR;

}
