package crfa.app.domain;

public enum DAppType {

    MINT_ONLY,
    SPEND_ONLY,
    MINT_AND_SPEND;

    public boolean hasSpend() {
        return this == SPEND_ONLY || this == MINT_AND_SPEND;
    }

}
