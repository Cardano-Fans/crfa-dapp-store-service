package crfa.app.domain;

import com.j256.ormlite.field.DataType;
import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;
import io.micronaut.core.annotation.Nullable;
import lombok.*;

import java.util.Date;

// table to represent dApp release

@Builder
@DatabaseTable(tableName = "dapp_release_epoch")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class DAppReleaseEpoch implements EpochGatherable {

    // primary id
    @DatabaseField(id = true, canBeNull = false, index = true, columnName = "id")
    String id; // e.g, CWpU1DBj.V1.365

    @DatabaseField(canBeNull = false, index = true, columnName = "key")
    String key; // e.g, CWpU1DBj.V1

    // foreign id
    @DatabaseField(canBeNull = false, index = true, columnName = "dapp_id")
    String dappId; // e.g, CWpU1DBj

    @DatabaseField(canBeNull = false, index = true, columnName = "epoch_no")
    int epochNo; // e.g, 365

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

    @DatabaseField(columnName = "inflows_outflows")
    @Nullable
    Long inflowsOutflows;

    @DatabaseField(columnName = "spend_unique_accounts")
    @Nullable
    Integer spendUniqueAccounts;

    @DatabaseField(canBeNull = false, columnName = "spend_transactions")
    Long spendTransactions;

    @DatabaseField(canBeNull = false, columnName = "mint_transactions")
    Long mintTransactions;

    @DatabaseField(canBeNull = false, columnName = "transactions")
    Long transactions;

    @DatabaseField(columnName = "spend_volume")
    @Nullable
    Long spendVolume;

    @DatabaseField(columnName = "spend_trx_fees")
    @Nullable
    Long spendTrxFees;

    @DatabaseField(columnName = "spend_trx_sizes")
    @Nullable
    Long spendTrxSizes;

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

    @DatabaseField(canBeNull = false, columnName = "closed_epoch", defaultValue = "false")
    boolean closedEpoch;

    @DatabaseField(canBeNull = false, columnName = "update_time", dataType = DataType.DATE_STRING)
    Date updateTime;

    public boolean isLatestVersion(float maxReleaseVersion) {
        return Float.compare(maxReleaseVersion, releaseNumber) == 0;
    }

}
