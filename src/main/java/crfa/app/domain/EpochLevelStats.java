package crfa.app.domain;

import io.micronaut.core.annotation.Nullable;
import lombok.Builder;
import lombok.Getter;

import static crfa.app.utils.MoreMath.safeDivision;

@Builder
@Getter
public final class EpochLevelStats {

    int startEpoch;

    int endEpoch;

    @Nullable
    Long volume; // for mint scripts we don't have scripts locked value

    @Nullable
    Long inflowsOutflows;

    @Nullable
    Long spendTrxFees;

    @Nullable
    @Deprecated
    Long trxFees;

    @Nullable
    Long spendTrxSizes;

    @Nullable // - null for script based only dapps
    Integer uniqueAccounts;

    // unified for transactionsCount and scriptInvocationsCount
    Long trxCount;

    boolean closed;

    public @Nullable Double getAvgTrxFee() {
        return safeDivision(spendTrxFees, trxCount);
    }

    public @Nullable Double getAvgTrxSize() {
        return safeDivision(spendTrxSizes, trxCount);
    }

    public static EpochLevelStats zero(int startEpochNo, int endEpochNo) {
        return EpochLevelStats.builder()
                .startEpoch(startEpochNo)
                .endEpoch(endEpochNo)
                .spendTrxFees(0L)
                .spendTrxSizes(0L)
                .closed(true)
                .volume(0L)
                .trxCount(0L)
                .uniqueAccounts(0)
                .inflowsOutflows(0L)
                .build();
    }

}
