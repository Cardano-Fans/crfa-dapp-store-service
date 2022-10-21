package crfa.app.domain;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Builder
@Getter
@AllArgsConstructor
public class EpochDelta {

    int fromEpoch;
    int toEpoch;
    Long volumeDiff;
    Double volumeDiffPerc;
    Long feesDiff;
    Double feesDiffPerc;
    Double avgFeesDiff;
    Double avgFeesDiffPerc;
    Long inflowsOutflowsDiff;
    Double inflowsOutflowsDiffPerc;
    Integer uniqueAccountsDiff;
    Double uniqueAccountsDiffPerc;
    Long trxCountDiff;
    Double trxCountDiffPerc;
    Double activityDiffPerc;

}
