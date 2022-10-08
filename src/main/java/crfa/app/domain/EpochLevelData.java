package crfa.app.domain;

import crfa.app.resource.model.EpochLevelStats;
import lombok.Builder;
import lombok.Getter;

import java.util.Map;
import java.util.Optional;

@Builder
@Getter
public class EpochLevelData {

    private Optional<EpochDelta> lastQuarterDeltaWithOnlyClosedEpochs;
    private Optional<EpochDelta> lastMonthDeltaWithOnlyClosedEpochs;
    private Optional<EpochDelta> lastEpochDeltaWithOnlyClosedEpochs;
    private Optional<Map<Integer, EpochLevelStats>> epochData;

}
