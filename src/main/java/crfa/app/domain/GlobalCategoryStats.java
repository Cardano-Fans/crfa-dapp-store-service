package crfa.app.domain;

import com.j256.ormlite.field.DataType;
import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;
import lombok.*;

import java.util.Date;

@Builder
@DatabaseTable(tableName = "global_category_stats")
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class GlobalCategoryStats {

    @DatabaseField(canBeNull = false, columnName = "category_type", id = true)
    private String categoryType;

    @DatabaseField(canBeNull = false, columnName = "scripts_locked")
    private long scriptsLocked;

    @DatabaseField(canBeNull = false, columnName = "trx_count")
    private long trxCount;

    @DatabaseField(canBeNull = false, columnName = "volume")
    private long volume;

    @DatabaseField(canBeNull = false, columnName = "fees")
    private long fees;

    @DatabaseField(canBeNull = false, columnName = "avg_fee")
    private double avgFee;

    @DatabaseField(canBeNull = false, columnName = "dapps")
    private int dapps;

//    @DatabaseField(canBeNull = false, columnName = "unique_accounts")
//    private long uniqueAccounts;

    @DatabaseField(canBeNull = false, columnName = "update_time", dataType = DataType.DATE_STRING)
    Date updateTime;

}
