package crfa.app.domain;

import io.micronaut.core.annotation.Nullable;

import static crfa.app.utils.MoreMath.safeAdd;
import static crfa.app.utils.MoreMath.safeDivision;

public interface EpochGatherable {

   int getEpochNo();

   @Nullable
   Long getSpendVolume();

   @Nullable
   Long getSpendTrxFees();

    @Nullable
   Long getSpendTrxSizes();

    @Nullable
   Long getInflowsOutflows();

   @Nullable
   Integer getSpendUniqueAccounts();

   @Nullable
   Long getSpendTransactions();

    @Nullable
    Long getMintTransactions();

    boolean isClosedEpoch();

   @Nullable default Double getAvgTrxFee() {
       return safeDivision(getSpendTrxFees(), getSpendTransactions());
   }

  @Nullable default Double getAvgTrxSize() {
       return safeDivision(getSpendTrxSizes(), getSpendTransactions());
   }

   default Long getTransactions() {
       return safeAdd(getMintTransactions(), getSpendTransactions());
   }

}
