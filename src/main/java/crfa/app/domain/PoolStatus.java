package crfa.app.domain;

public enum PoolStatus {
    NOT_STAKED,
    NOT_FOUND, // we tried but could not find it
    STAKED, // e.g. no ticker
    SYNC_IN_PROGRESS,
    NA // not applicable, e.g. MINT address
}
