package crfa.app.domain;

import com.j256.ormlite.field.DataType;
import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;
import lombok.*;

import java.util.Date;

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

    @DatabaseField(canBeNull = false, columnName = "total_unique_accounts")
    private long totalUniqueAccounts;

    @DatabaseField(canBeNull = false, columnName = "update_time", dataType = DataType.DATE_STRING)
    Date updateTime;

}
