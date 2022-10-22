package crfa.app.domain;

import io.micronaut.core.annotation.Nullable;
import lombok.Builder;
import lombok.Getter;

@Builder
@Getter
public final class EpochLevelStats {

    @Nullable
    Long volume; // for mint scripts we don't have scripts locked value

    @Nullable
    Long fees;

    @Nullable
    Double avgFee;

    @Nullable
    Double avgTrxSize;

    @Nullable
    Long inflowsOutflows;

    @Nullable // - null for script based only dapps
    Integer uniqueAccounts;

    // unified for transactionsCount and scriptInvocationsCount
    Long trxCount;

    boolean closed;

}
