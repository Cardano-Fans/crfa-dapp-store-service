package crfa.app.resource.model;

import lombok.*;

@Builder
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class GlobalCategoryStatsResult {

    private long scriptsLocked;

    private long trxCount;

    private long volume;

    private long fees;

    private double avgFee;

    private int dapps;

//    @DatabaseField(canBeNull = false, columnName = "unique_accounts")
//    private long uniqueAccounts;

}
