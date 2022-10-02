package crfa.app.domain;

public record EpochDelta(int fromEpoch,
                         int toEpoch,
                         long volumeDiff,
                         float volumeDiffPerc,
                         long inflowsOutflowsDiff,
                         float inflowsOutflowsDiffPerc,
                         int uniqueAccountsDiff,
                         float uniqueAccountsDiffPerc,
                         long trxCountDiff,
                         float trxCountDiffPerc
) {

}
