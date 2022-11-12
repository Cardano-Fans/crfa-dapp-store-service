package crfa.app.resource.model;

import lombok.*;

@Builder
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class GlobalCategoryEpochStatsResult {

    private String id;

    private int epochNo;

    private String categoryType;

    private long inflowsOutflows;

    private long trxCount;

    private long volume;

    private long fees;

    private long uniqueAccounts;

    private Double avgTrxFee;

    private Double avgTrxSize;

    private int dapps;

}
