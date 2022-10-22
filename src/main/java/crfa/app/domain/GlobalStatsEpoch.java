package crfa.app.domain;

import com.j256.ormlite.field.DataType;
import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;
import io.micronaut.core.annotation.Nullable;
import lombok.*;

import java.util.Date;

import static crfa.app.utils.MoreMath.safeDivision;

@Builder
@DatabaseTable(tableName = "global_stats_epoch")
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class GlobalStatsEpoch {

    @DatabaseField(canBeNull = false, columnName = "epoch_no", id = true)
    private int epochNo;

    @DatabaseField(canBeNull = false, columnName = "total_inflows_outflows")
    private long inflowsOutflows;

    @DatabaseField(canBeNull = false, columnName = "total_trx_count")
    private long totalTrxCount;

    @DatabaseField(canBeNull = false, columnName = "total_volume")
    private long totalVolume;

    @DatabaseField(canBeNull = false, columnName = "total_fees")
    private long totalFees;

    @DatabaseField(canBeNull = false, columnName = "total_trx_sizes")
    private long totalTrxSizes;

    @DatabaseField(canBeNull = false, columnName = "total_unique_accounts")
    private long totalUniqueAccounts;

    @DatabaseField(canBeNull = false, columnName = "update_time", dataType = DataType.DATE_STRING)
    Date updateTime;

    public @Nullable Double getAvgFee() {
        return safeDivision(totalFees, totalTrxCount);
    }

    public @Nullable Double getAvgTrxSize() {
        return safeDivision(totalTrxSizes, totalTrxCount);
    }

}
