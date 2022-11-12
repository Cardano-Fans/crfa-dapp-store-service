package crfa.app.resource.model;

import crfa.app.domain.EpochLevelStats;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.Map;
import java.util.Optional;

@Builder
@Getter
@AllArgsConstructor
@NoArgsConstructor
public class EpochLevelDataResult {

    private Optional<EpochLevelResult> lastQuarterDeltaWithOnlyClosedEpochs;
    private Optional<EpochLevelResult> lastMonthDeltaWithOnlyClosedEpochs;
    private Optional<EpochLevelResult> lastEpochDeltaWithOnlyClosedEpochs;
    private Optional<EpochLevelResult> allEpochDeltaWithOnlyClosedEpochs;
    private Optional<Map<Integer, EpochLevelStats>> epochData; // for now no results class for it

}
