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
    Long volume;

    @DatabaseField(columnName = "fees")
    @Nullable
    Long fees;

    @DatabaseField(columnName = "trx_sizes")
    @Nullable
    Long trxSizes;

    @DatabaseField(columnName = "inflows_outflows")
    @Nullable
    Long inflowsOutflows; // for mint scripts we don't have scripts locked value

    @DatabaseField(columnName = "unique_accounts")
    @Nullable
    Integer uniqueAccounts;

    // total number of all script innovations belonging to this dApp
    @DatabaseField(canBeNull = false, columnName = "script_invocations", index = true)
    Long scriptInvocationsCount;

    @DatabaseField(canBeNull = false, columnName = "update_time", dataType = DataType.DATE_STRING)
    Date updateTime;

    @DatabaseField(canBeNull = false, columnName = "closed_epoch", defaultValue = "false")
    boolean closedEpoch;

    @DatabaseField(columnName = "plutus_version")
    int plutusVersion;

    public @Nullable Double getAvgFee() {
        return safeDivision(fees, scriptInvocationsCount);
    }

    public @Nullable Double getAvgTrxSize() {
        return safeDivision(trxSizes, scriptInvocationsCount);
    }

}
