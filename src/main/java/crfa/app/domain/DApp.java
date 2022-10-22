package crfa.app.domain;

import com.j256.ormlite.field.DataType;
import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;
import io.micronaut.core.annotation.Nullable;
import lombok.*;

import java.util.Date;

import static crfa.app.utils.MoreMath.safeDivision;

// table to represent dapp, either last version or all versions

@Builder
@DatabaseTable(tableName = "dapp")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class DApp {

    @DatabaseField(canBeNull = false, index = true, id = true)
    String id; // e.g, CWpU1DBj

    @DatabaseField(canBeNull = false, index = true)
    String name; // e.g, Meld

    @DatabaseField(canBeNull = false, columnName = "dapp_type")
    DAppType dAppType;

    @DatabaseField(canBeNull = false)
    String link; // project link

    @DatabaseField(canBeNull = false)
    String icon;

    @DatabaseField
    String twitter; // twitter link

    @DatabaseField(canBeNull = false)
    String category; // e.g. DEFI

    @DatabaseField(columnName = "sub_category")
    String subCategory; // e.g. LENDING_DAPP

    @DatabaseField(columnName = "scripts_locked")
    @Nullable
    Long scriptsLocked;

    @DatabaseField(columnName = "volume")
    @Nullable
    Long volume;

    @DatabaseField(columnName = "fees")
    @Nullable
    Long fees;

    @DatabaseField(columnName = "trx_sizes")
    @Nullable
    Long trxSizes;

    @DatabaseField(columnName = "unique_accounts")
    @Nullable
    Integer uniqueAccounts;

    // total number of all script innovations belonging to this dApp
    @DatabaseField(canBeNull = false, columnName = "script_invocations")
    Long scriptInvocationsCount;

    @Nullable
    @DatabaseField(columnName = "last_version_audit_link")
    String lastVersionAuditLink;

    @Nullable
    @DatabaseField(columnName = "last_version_open_source_link")
    String lastVersionOpenSourceLink;

    @DatabaseField(canBeNull = false, columnName = "update_time", dataType = DataType.DATE_STRING)
    Date updateTime;

    public @Nullable Double getAvgFee() {
        return safeDivision(fees, scriptInvocationsCount);
    }

    public @Nullable Double getAvgTrxSize() {
        return safeDivision(trxSizes, scriptInvocationsCount);
    }

}
