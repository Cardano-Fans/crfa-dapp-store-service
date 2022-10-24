package crfa.app.resource.model;

import lombok.*;

@Builder
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class GlobalCategoryStatsResult {

    private long balance;

    private long trxCount;

    private long volume;

    private long fees;

    private Double avgTrxFee;

    private Double avgTrxSize;

    private int dapps;

//  TODO
//  @DatabaseField(canBeNull = false, columnName = "spend_unique_accounts")
//  private long spendUniqueAccounts;

}
