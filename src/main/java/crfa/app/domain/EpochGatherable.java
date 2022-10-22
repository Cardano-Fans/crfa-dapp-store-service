package crfa.app.domain;

import io.micronaut.core.annotation.Nullable;

public interface EpochGatherable {

   int getEpochNo();

   @Nullable
   Long getVolume();

   @Nullable
   Long getFees();

   @Nullable
   Long getInflowsOutflows();

   @Nullable
   Integer getUniqueAccounts();

   @Nullable
   Long getScriptInvocationsCount();

   boolean isClosedEpoch();

   @javax.annotation.Nullable Double getAvgFee();

    @javax.annotation.Nullable Double getAvgTrxSize();

}
