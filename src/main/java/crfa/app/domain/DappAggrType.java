package crfa.app.domain;

public enum DappAggrType {

    LAST, // last version only
    ALL; // all dapp versions aggregated

    public static DappAggrType def() {
        return ALL;
    }

}
