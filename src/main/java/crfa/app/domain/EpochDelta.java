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
    Float volumeDiffPerc;
    Long feesDiff;
    Float feesDiffPerc;
    Long inflowsOutflowsDiff;
    Float inflowsOutflowsDiffPerc;
    Integer uniqueAccountsDiff;
    Float uniqueAccountsDiffPerc;
    Long trxCountDiff;
    Float trxCountDiffPerc;
    Double activityDiffPerc;
}
