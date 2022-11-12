package crfa.app.domain;

import lombok.val;

import static crfa.app.domain.EraName.ALONZO;

public enum SnapshotType {

    ALL, ONE, SIX, EIGHTEEN;

    public int startEpoch(int epochNo) {
        val start = Eras.epochForEra(ALONZO);

        return switch (this) {
            case ALL -> start;
            case ONE -> epochNo - 1;
            case SIX -> epochNo - 6;
            case EIGHTEEN -> epochNo - 18;
        };
    }

}
