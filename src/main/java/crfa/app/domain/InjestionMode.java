package crfa.app.domain;

public enum InjestionMode {
    FULL, // standard plus FULL epoch level data
    CURRENT_EPOCH_AND_AGGREGATES, // standard aggregates plus last epoch on epoch level
    WITHOUT_EPOCHS_ONLY_AGGREGATES // standard only (aggregates), without any epoch level data
}
