package crfa.app.domain;

import com.j256.ormlite.field.DataType;
import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;
import io.micronaut.core.annotation.Nullable;
import lombok.*;

import java.util.Date;

// table to represent dapp, either last version or all versions

@Builder
@DatabaseTable(tableName = "dapp_epoch")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class DAppEpoch implements EpochGatherable {

    @DatabaseField(canBeNull = false, index = true, id = true)
    String id; // e.g, CWpU1DBj.365

    @DatabaseField(canBeNull = false, index = true, columnName = "dapp_id")
    String dappId;  // e.g, CWpU1DBj

    @DatabaseField(canBeNull = false, index = true, columnName = "epoch_no")
    int epochNo;

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

    @DatabaseField(columnName = "spend_volume")
    @Nullable
    Long spendVolume;

    @DatabaseField(columnName = "spend_trx_fees")
    @Nullable
    Long spendTrxFees;

    @DatabaseField(columnName = "spend_trx_sizes")
    @Nullable
    Long spendTrxSizes;

    @DatabaseField(columnName = "spend_unique_accounts")
    @Nullable
    Integer spendUniqueAccounts;

    @DatabaseField(canBeNull = false, columnName = "spend_transactions")
    @Nullable Long spendTransactions;

    @DatabaseField(canBeNull = false, columnName = "mint_transactions")
    @Nullable Long mintTransactions;

    @DatabaseField(canBeNull = false, columnName = "transactions")
    Long transactions;

    @DatabaseField(columnName = "inflows_outflows")
    @Nullable
    Long inflowsOutflows;

    @Nullable
    @DatabaseField(columnName = "last_version_audit_link")
    String lastVersionAuditLink;

    @Nullable
    @DatabaseField(columnName = "last_version_open_source_link")
    String lastVersionOpenSourceLink;

    @DatabaseField(canBeNull = false, columnName = "closed_epoch", defaultValue = "false")
    boolean closedEpoch;

    @DatabaseField(canBeNull = false, columnName = "update_time", dataType = DataType.DATE_STRING)
    Date updateTime;

}
