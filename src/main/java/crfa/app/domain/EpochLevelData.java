package crfa.app.domain;

import lombok.Builder;
import lombok.Getter;

import java.util.Map;
import java.util.Optional;

@Builder
@Getter
public class EpochLevelData {

    private Optional<EpochLevelDiff> lastQuarterDeltaWithOnlyClosedEpochs;
    private Optional<EpochLevelDiff> lastMonthDeltaWithOnlyClosedEpochs;
    private Optional<EpochLevelDiff> lastEpochDeltaWithOnlyClosedEpochs;
    //private Optional<EpochLevelDiff> allEpochDeltaWithOnlyClosedEpochs;
    private Optional<Map<Integer, EpochLevelStats>> epochData;

}
