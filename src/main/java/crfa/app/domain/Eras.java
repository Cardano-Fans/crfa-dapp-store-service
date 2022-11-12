package crfa.app.domain;

import com.google.common.collect.ContiguousSet;
import com.google.common.collect.DiscreteDomain;
import com.google.common.collect.Range;
import lombok.val;

import java.util.Set;

import static crfa.app.domain.EraName.*;

public class Eras {

    // including lower and upper bounds
    public static Set<Integer> epochsBetween(EraName fromEra, int toEpochNo) {
        val range = allEpochNumbersBetween(fromEra, toEpochNo);

        return ContiguousSet.create(range, DiscreteDomain.integers());
    }

    public static Set<Integer> epochsBetween(EraName from, EraName to) {
        int fromEpochNo = epochForEra(from);
        int toEpochNo = epochForEra(to);

        return ContiguousSet.create(Range.closed(fromEpochNo, toEpochNo), DiscreteDomain.integers());
    }

    public static Set<Integer> epochsBetween(int fromEpochNo, int toEpochNo) {
        return ContiguousSet.create(Range.closed(fromEpochNo, toEpochNo), DiscreteDomain.integers());
    }

    private static Range<Integer> allEpochNumbersBetween(EraName fromEra, int toEpochNo) {
        int fromEpochNo = epochForEra(fromEra);

        return Range.closed(fromEpochNo, toEpochNo);
    }

    public static int epochForEra(EraName eraName) {
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
        if (eraName == BABBAGE) {
            return 365;
        }

        throw new RuntimeException("era not mapped yet:" + eraName);
    }

}
