package crfa.app.domain;


import crfa.app.utils.MoreDouble;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.val;

import java.util.Objects;
import java.util.Optional;
import java.util.stream.Stream;

import static crfa.app.utils.MoreMath.*;

@Builder
@Getter
@AllArgsConstructor
public class EpochLevelDiff {

    private EpochLevelStats from; // should be snapshot here
    private EpochLevelStats to; // should be snapshot here

    private EpochLevelStats snapshot;

    public Long volumeDiff() {
        return safeSubtraction(to.getVolume(), from.getVolume());
    }

    public Long feesDiff() {
        return safeSubtraction(to.getSpendTrxFees(), from.getSpendTrxFees());
    }

    public Double avgFeesDiff() {
        return safeSubtraction(to.getAvgTrxFee(), from.getAvgTrxFee());
    }

    public Long inflowsOutflowsDiff() {
        return safeSubtraction(to.getInflowsOutflows(), from.getInflowsOutflows());
    }

    public Integer uniqueAccountsDiff() {
        return safeSubtraction(to.getUniqueAccounts(), from.getUniqueAccounts());
    }

    public Long trxCountDiff() {
        return safeSubtraction(to.getTrxCount(), from.getTrxCount());
    }

    public Double avgTrxSizeDiff() {
        return safeSubtraction(to.getAvgTrxSize(), from.getAvgTrxSize());
    }

    public Double volumeDiffPerc() {
        return safeMultiplication(safeDivision(volumeDiff(), from.getVolume()), (double) 100);
    }

    public Double feesDiffPerc() {
        return safeMultiplication(safeDivision(feesDiff(), from.getSpendTrxFees()), (double) 100);
    }

    public Double avgFeesDiffPerc() {
        return safeMultiplication(safeDivision(avgFeesDiff(), from.getAvgTrxFee()), (double) 100);
    }

    public Double inflowsOutflowsDiffPerc() {
        return safeMultiplication(safeDivision(inflowsOutflowsDiff(), from.getInflowsOutflows()), (double) 100);
    }

    public Double uniqueAccountsDiffPerc() {
        return safeMultiplication(safeDivision(uniqueAccountsDiff(), from.getUniqueAccounts()), (double) 100);
    }

    public Double trxCountDiffPerc() {
        return safeMultiplication(safeDivision(trxCountDiff(), from.getTrxCount()), (double) 100);
    }

    public Double avgTrxSizeDiffPerc() {
        return safeMultiplication(safeDivision(avgTrxSizeDiff(), from.getAvgTrxSize()), (double) 100);
    }

    public Optional<Double> activityDiffPerc() {
        val d = Stream.of(volumeDiffPerc(), uniqueAccountsDiffPerc(), trxCountDiffPerc())
                .filter(Objects::nonNull).mapToDouble(Double::doubleValue)
                .average()
                .getAsDouble();


        if (MoreDouble.isInvalid(d)) {
            return Optional.empty();
        }

        return Optional.of(d);
    }

}