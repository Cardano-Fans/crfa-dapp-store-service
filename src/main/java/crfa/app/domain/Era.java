package crfa.app.domain;

import com.google.common.collect.Range;
import lombok.AllArgsConstructor;

import static crfa.app.domain.EraName.*;

@AllArgsConstructor
public class Era {

    private final EraName eraName;

    // including lower and upper bounds
    public Range<Integer> allEpochNumbersBetween(int toEpochNo) {
        int fromEpochNo = epochForEra(eraName);

        return Range.closed(fromEpochNo, toEpochNo);
    }

    private int epochForEra(EraName eraName) {
        if (eraName == SHELLEY) {
            return 208;
        }
        if (eraName == ALEGRA) {
            return 236;
        }
        if (eraName == MARY) {
            return 251;
        }
        if (eraName == ALONZO) {
            return 290;
        }

        throw new RuntimeException("era not mapped yet:" + eraName);
    }

}
