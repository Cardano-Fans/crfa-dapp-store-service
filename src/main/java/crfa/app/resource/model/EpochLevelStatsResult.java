package crfa.app.resource.model;

import crfa.app.domain.EpochLevelStats;
import io.micronaut.core.annotation.Nullable;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Builder
@Getter
@Setter
public class EpochLevelStatsResult {

    @Nullable
    Long volume; // for mint scripts we don't have scripts locked value

    @Nullable
    Long inflowsOutflows;

    @Nullable
    @Deprecated
    Long trxFees;

    @Nullable
    Long spendTrxFees;

    @Nullable
    Long spendTrxSizes;

    @Nullable
    Double avgTrxFee;

    @Nullable
    Double avgTrxSize;

    @Nullable // - null for script based only dapps
    Integer uniqueAccounts;

    // unified for transactionsCount and scriptInvocationsCount
    Long trxCount;

    boolean closed;

    public static EpochLevelStatsResult create(EpochLevelStats epochLevelStats) {
        return EpochLevelStatsResult.builder()
                .closed(epochLevelStats.isClosed())
                .avgTrxFee(epochLevelStats.getAvgTrxFee())
                .avgTrxSize(epochLevelStats.getAvgTrxSize())
                .inflowsOutflows(epochLevelStats.getInflowsOutflows())
                .trxCount(epochLevelStats.getTrxCount())
                .volume(epochLevelStats.getVolume())
                .uniqueAccounts(epochLevelStats.getUniqueAccounts())
                .spendTrxFees(epochLevelStats.getSpendTrxFees())
                .trxFees(epochLevelStats.getSpendTrxFees()) // deprecated
                .build();
    }

}
