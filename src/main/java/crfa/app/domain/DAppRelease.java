package crfa.app.domain;

import com.j256.ormlite.field.DataType;
import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;
import crfa.app.client.metadata.DappReleaseItem;
import crfa.app.client.metadata.DappSearchItem;
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

    @DatabaseField(columnName = "balance")
    @Nullable
    Long balance;

    @DatabaseField(columnName = "spend_unique_accounts")
    @Nullable
    Integer spendUniqueAccounts;

    @DatabaseField(columnName = "spend_unique_accounts_last_epoch")
    @Nullable
    Integer spendUniqueAccounts_lastEpoch;

    @DatabaseField(columnName = "spend_unique_accounts_six_epochs_ago")
    @Nullable
    Integer spendUniqueAccounts_six_epochs_ago;

    @DatabaseField(columnName = "spend_unique_accounts_eighteen_epochs_ago")
    @Nullable
    Integer spendUniqueAccounts_eighteen_epochs_ago;

    @DatabaseField(canBeNull = false, columnName = "spend_transactions")
    @Nullable Long spendTransactions;

    @DatabaseField(canBeNull = false, columnName = "mint_transactions")
    @Nullable Long mintTransactions;

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

    @DatabaseField(canBeNull = false, columnName = "update_time", dataType = DataType.DATE_STRING)
    Date updateTime;

    public boolean isLatestVersion(float maxReleaseVersion) {
        return Float.compare(maxReleaseVersion, releaseNumber) == 0;
    }

    public @Nullable Double getAvgTrxFee() {
        return safeDivision(spendTrxFees, spendTransactions);
    }

    public @Nullable Double getAvgTrxSize() {
        return safeDivision(spendTrxSizes, spendTransactions);
    }

    public Long getTransactionsCount() {
        return spendTransactions + mintTransactions;
    }

    public static String createId(DappSearchItem dappSearchItem, DappReleaseItem dappReleaseItem) {
        return String.format("%s.%.1f", dappSearchItem.getId(), dappReleaseItem.getReleaseNumber());
    }

}
