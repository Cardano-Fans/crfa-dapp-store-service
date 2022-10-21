package crfa.app.domain;

import com.j256.ormlite.field.DataType;
import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;
import io.micronaut.core.annotation.Nullable;
import lombok.*;

import java.util.Date;

import static crfa.app.utils.MoreMath.safeDivision;

// table to represent dApp release

@Builder
@DatabaseTable(tableName = "dapp_release")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class DAppRelease {

    @DatabaseField(id = true, canBeNull = false, index = true, columnName = "id")
    String id; // e.g, CWpU1DBj.V1

    @DatabaseField(canBeNull = false, index = true, columnName = "dapp_id")
    String dappId; // e.g, CWpU1DBj

    @DatabaseField(canBeNull = false, index = true)
    String name; // e.g, Meld

    @DatabaseField(canBeNull = false, index = true, columnName = "full_name")
    String fullName;

    @DatabaseField(canBeNull = false, index = true, columnName = "release_number")
    float releaseNumber;

    @DatabaseField(canBeNull = false, index = true, columnName = "release_name")
    String releaseName;

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

    @DatabaseField(columnName = "unique_accounts")
    @Nullable
    Integer uniqueAccounts;

    // total number of all script innovations belonging to this dApp
    @DatabaseField(canBeNull = false, columnName = "script_invocations")
    Long scriptInvocationsCount;

    @DatabaseField(columnName = "volume")
    @Nullable
    Long volume;

    @DatabaseField(columnName = "fees")
    @Nullable
    Long fees;

    @DatabaseField(canBeNull = false, columnName = "update_time", dataType = DataType.DATE_STRING)
    Date updateTime;

    @DatabaseField(columnName = "audit_auditor")
    @Nullable
    String auditor;

    @DatabaseField(columnName = "audit_link")
    @Nullable
    String auditLink;

    @DatabaseField(columnName = "contract_open_source")
    @Nullable
    Boolean contractOpenSource;

    @DatabaseField(columnName = "contract_link")
    @Nullable
    String contractLink;

    public boolean isLatestVersion(float maxReleaseVersion) {
        return Float.compare(maxReleaseVersion, releaseNumber) == 0;
    }

    public @Nullable Double getAvgFee() {
        return safeDivision(fees, scriptInvocationsCount);
    }

}
