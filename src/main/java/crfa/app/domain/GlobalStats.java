package crfa.app.domain;

import com.j256.ormlite.field.DataType;
import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;
import io.micronaut.core.annotation.Nullable;
import lombok.*;

import java.math.BigDecimal;
import java.util.Date;

import static crfa.app.utils.MoreMath.safeDivision;

@Builder
@DatabaseTable(tableName = "global_stats")
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class GlobalStats {

    @DatabaseField(canBeNull = false, columnName = "id", id = true)
    private String id;

    @DatabaseField(canBeNull = false, columnName = "balance")
    private long balance;

    @DatabaseField(canBeNull = false, columnName = "spend_transactions")
    private long spendTransactions;

    @DatabaseField(canBeNull = false, columnName = "mint_transactions")
    private long mintTransactions;

    @DatabaseField(canBeNull = false, columnName = "transactions")
    private long transactions;

    @DatabaseField(canBeNull = false, columnName = "spend_volume")
    private long spendVolume;

    @DatabaseField(canBeNull = false, columnName = "spend_trx_fees")
    private long spendTrxFees;

    @DatabaseField(canBeNull = false, columnName = "spend_trx_sizes")
    private long spendTrxSizes;

    @DatabaseField(canBeNull = false, columnName = "spend_unique_accounts")
    private long spendUniqueAccounts;

    @DatabaseField(canBeNull = false, columnName = "dapps")
    private int dapps;

    @DatabaseField(canBeNull = false, columnName = "ada_price_eur")
    private BigDecimal adaPriceEUR;

    @DatabaseField(canBeNull = false, columnName = "ada_price_usd")
    private BigDecimal adaPriceUSD;

    @DatabaseField(canBeNull = false, columnName = "update_time", dataType = DataType.DATE_STRING)
    Date updateTime;

    public @Nullable Double getAvgTrxFee() {
        return safeDivision(spendTrxFees, spendTransactions);
    }

    public @Nullable Double getAvgTrxSize() {
        return safeDivision(spendTrxSizes, spendTransactions);
    }

    public Long getTransactionsCount() {
        return spendTransactions + mintTransactions;
    }

}
