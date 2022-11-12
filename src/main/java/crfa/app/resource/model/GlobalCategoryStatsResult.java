package crfa.app.resource.model;

import lombok.*;

@Builder
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class GlobalCategoryStatsResult {

    private String category;

    private long balance;

    private long trxCount;

    private long volume;

    private long fees;

    private long uniqueAccounts;

    private Double avgTrxFee;

    private Double avgTrxSize;

    private int dapps;

}
