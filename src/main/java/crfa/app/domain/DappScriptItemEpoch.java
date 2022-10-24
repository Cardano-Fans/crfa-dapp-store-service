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
@DatabaseTable(tableName = "dapp_script_item_epoch")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class DappScriptItemEpoch implements EpochGatherable {

    @DatabaseField(id = true, canBeNull = false)
    String id; // either script hash or mintPolicyId

    @DatabaseField(id = false, canBeNull = false, index = true, columnName = "hash")
    String hash; // either script hash or mintPolicyId

    @DatabaseField(canBeNull = false, index = true, columnName = "dapp_id")
    String dappId; // dapp id

    @DatabaseField(canBeNull = false, index = true, columnName = "release_key")
    String releaseKey; // e.g, CWpU1DBj.1.0 // foreign key to dapp_release

    @DatabaseField(canBeNull = false, index = true, columnName = "epoch_no")
    int epochNo;

    @DatabaseField(canBeNull = false, columnName = "script_type")
    ScriptType scriptType;

    @DatabaseField(columnName = "name", index = true)
    @Nullable
    String name;

    @DatabaseField(canBeNull = false, columnName = "version", index = true)
    int version;

    @DatabaseField(columnName = "mint_policy_id")
    @Nullable
    String mintPolicyID;

    @DatabaseField(columnName = "volume")
    @Nullable
    Long volume; // ADA

    @DatabaseField(columnName = "trx_fees")
    @Nullable
    Long trxFees; // ADA

    @DatabaseField(columnName = "trx_sizes")
    @Nullable
    Long trxSizes; // bytes

    @DatabaseField(columnName = "inflows_outflows")
    @Nullable
    Long inflowsOutflows; // ADA

    @DatabaseField(columnName = "unique_accounts")
    @Nullable
    Integer uniqueAccounts;

    @DatabaseField(canBeNull = false, columnName = "transactions", index = true)
    Long transactions;

    @DatabaseField(canBeNull = false, columnName = "closed_epoch", defaultValue = "false")
    boolean closedEpoch;

    @DatabaseField(columnName = "plutus_version")
    int plutusVersion;

    @DatabaseField(canBeNull = false, columnName = "update_time", dataType = DataType.DATE_STRING)
    Date updateTime;

    @Override
    public @Nullable Long getSpendVolume() {
        return scriptType == ScriptType.SPEND ? this.volume : 0L;
    }

    @Override
    public Long getSpendTrxFees() {
        return scriptType == ScriptType.SPEND ? this.trxFees : 0L;
    }

    @Override
    public Long getSpendTrxSizes() {
        return scriptType == ScriptType.SPEND ? this.trxSizes : 0L;
    }

    @Override
    public Integer getSpendUniqueAccounts() {
        return scriptType == ScriptType.SPEND ? this.uniqueAccounts : 0;
    }

    @Override
    public Long getSpendTransactions() {
        return scriptType == ScriptType.SPEND ? transactions : 0;
    }

    @Override
    public Long getMintTransactions() {
        return scriptType == ScriptType.MINT ? transactions : 0;
    }

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
