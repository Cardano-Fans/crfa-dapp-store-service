package crfa.app.domain;

import com.j256.ormlite.field.DataType;
import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;
import lombok.*;

import java.math.BigDecimal;
import java.util.Date;

@Builder
@DatabaseTable(tableName = "global_stats")
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class GlobalStats {

    @DatabaseField(canBeNull = false, columnName = "id", id = true)
    private String id;

    @DatabaseField(canBeNull = false, columnName = "total_scripts_locked")
    private long totalScriptsLocked;

    @DatabaseField(canBeNull = false, columnName = "total_trx_count")
    private long totalTrxCount;

    @DatabaseField(canBeNull = false, columnName = "total_volume")
    private long totalVolume;

    @DatabaseField(canBeNull = false, columnName = "total_fees")
    private long totalFees;

    @DatabaseField(canBeNull = false, columnName = "total_unique_accounts")
    private long totalUniqueAccounts;

    @DatabaseField(canBeNull = false, columnName = "total_dapps")
    private int totalDapps;

    @DatabaseField(canBeNull = false, columnName = "ada_price_eur")
    private BigDecimal adaPriceEUR;

    @DatabaseField(canBeNull = false, columnName = "ada_price_usd")
    private BigDecimal adaPriceUSD;

    @DatabaseField(canBeNull = false, columnName = "update_time", dataType = DataType.DATE_STRING)
    Date updateTime;

}
