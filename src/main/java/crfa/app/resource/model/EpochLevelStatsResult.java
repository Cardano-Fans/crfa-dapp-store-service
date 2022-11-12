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
//                .startEpoch(epochLevelStats.getStartEpoch())
//                .endEpoch(epochLevelStats.getEndEpoch())
                .avgTrxFee(epochLevelStats.getAvgTrxFee())
                .avgTrxSize(epochLevelStats.getAvgTrxSize())
                .inflowsOutflows(epochLevelStats.getInflowsOutflows())
                .trxCount(epochLevelStats.getTrxCount())
                .volume(epochLevelStats.getVolume())
                .uniqueAccounts(null) // TODO
                .spendTrxFees(epochLevelStats.getSpendTrxFees())
                .build();
    }

}
