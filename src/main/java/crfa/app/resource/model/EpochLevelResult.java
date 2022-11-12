package crfa.app.resource.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.util.Optional;

@Builder
@Getter
@AllArgsConstructor
public class EpochLevelResult {

        private int from;
        private int to;

        private EpochLevelStatsResult snapshot;

        private Optional<Double> activityDiffPerc;

  }
