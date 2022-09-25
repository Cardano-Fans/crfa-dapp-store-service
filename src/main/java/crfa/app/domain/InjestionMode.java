package crfa.app.domain;

public enum InjestionMode {
    FULL, // standard plus FULL epoch level data
    CURRENT_EPOCH, // standard aggregates plus last epoch on epoch level
    WITHOUT_EPOCHS // standard only, without any epoch level data
}
