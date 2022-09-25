package crfa.app.domain;

import com.j256.ormlite.field.DataType;
import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;
import io.micronaut.core.annotation.Nullable;
import lombok.*;

import java.util.Date;

// table to represent dApp release

@Builder
@DatabaseTable(tableName = "dapp_release")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class DAppRelease {

    @DatabaseField(id = true, canBeNull = false, index = true, columnName = "key")
    String key; // e.g, CWpU1DBj.V1

    @DatabaseField(canBeNull = false, index = true)
    String id; // e.g, CWpU1DBj

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

    @DatabaseField(columnName = "scripts_locked", index = true)
    @Nullable
    Long scriptsLocked;

    // total number of transactions for dApp since beginning
    @DatabaseField(columnName = "transactions_count", index = true)
    @Nullable // we don't always have contract calls, for instance for MINT scripts
    Long transactionsCount; // value across all scripts per dApp

    @DatabaseField(columnName = "unique_accounts")
    @Nullable
    Integer uniqueAccounts;

    // total number of all script innovations belonging to this dApp
    @DatabaseField(canBeNull = false, columnName = "script_invocations", index = true)
    Long scriptInvocationsCount;

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

}
