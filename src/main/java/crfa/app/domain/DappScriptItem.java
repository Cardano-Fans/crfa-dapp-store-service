package crfa.app.domain;

import com.j256.ormlite.field.DataType;
import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;
import io.micronaut.core.annotation.Nullable;
import lombok.*;

import java.util.Date;

import static crfa.app.utils.MoreMath.safeDivision;

// table to represent dapp release item, which is in fact on script level

@Builder
@DatabaseTable(tableName = "dapp_script_item")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class DappScriptItem {

    @DatabaseField(id = true, canBeNull = false, index = true, columnName = "hash")
    String hash; // either script hash or mintPolicyId

    @DatabaseField(canBeNull = false, index = true, columnName = "dapp_id")
    String dappId; // dapp id

    @DatabaseField(canBeNull = false, index = true, columnName = "release_key")
    String releaseKey; // e.g, CWpU1DBj.V1 // foreign key to dapp_release

    @DatabaseField(canBeNull = false, columnName = "script_type")
    ScriptType scriptType;

    @DatabaseField(columnName = "name")
    @Nullable
    String name;

    @DatabaseField(canBeNull = false, columnName = "version")
    int version;

    @DatabaseField(columnName = "mint_policy_id")
    @Nullable
    String mintPolicyID;

    @DatabaseField(columnName = "balance") // balance in ADA
    @Nullable
    Long balance; // ADA

    // total number of all script innovations belonging to this dApp
    @DatabaseField(canBeNull = false, columnName = "transactions")
    Long transactions;

    @DatabaseField(columnName = "unique_accounts")
    @Nullable
    Integer uniqueAccounts;

    @DatabaseField(columnName = "volume")
    @Nullable
    Long volume; // ADA

    @DatabaseField(columnName = "trx_fees")
    @Nullable
    Long trxFees; // ADA

    @DatabaseField(columnName = "pool_data")
    String pool; // pool data as json

    @DatabaseField(columnName = "trx_sizes")
    @Nullable
    Long trxSizes; // bytes

    @DatabaseField(columnName = "plutus_version")
    int plutusVersion;

    @DatabaseField(canBeNull = false, columnName = "update_time", dataType = DataType.DATE_STRING)
    Date updateTime;

    public @Nullable Double getAvgTrxFee() {
        return safeDivision(trxFees, transactions);
    }

    public @Nullable Double getAvgTrxSize() {
        return safeDivision(trxSizes, transactions);
    }

    public Long getTransactionsCount() {
        return transactions;
    }

}
