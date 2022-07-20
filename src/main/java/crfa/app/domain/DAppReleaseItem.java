package crfa.app.domain;

import com.j256.ormlite.field.DataType;
import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;
import io.micronaut.core.annotation.Nullable;
import lombok.*;

import java.util.Date;

@Builder
@DatabaseTable(tableName = "dapp_release_item")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class DAppReleaseItem {

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

//    @DatabaseField(canBeNull = false, index = true, columnName = "dapp_name")
//    String dappName; // e.g, Meld
//
//    @DatabaseField(canBeNull = false, index = true, columnName = "dapp_full_name")
//    String dappFullName;
//
//    @DatabaseField(canBeNull = false, index = true, columnName = "dapp_release_number")
//    float dappReleaseNumber;
//
//    @DatabaseField(canBeNull = false, index = true, columnName = "dapp_release_name")
//    String dappReleaseName;

//    @DatabaseField(canBeNull = false, columnName = "dapp_type")
//    DAppType dAppType;

    @DatabaseField(columnName = "contract_address")
    @Nullable
    String contractAddress;

    @DatabaseField(columnName = "mint_policy_id")
    @Nullable
    String mintPolicyID;

    @DatabaseField(columnName = "scripts_locked", index = true)
    @Nullable
    Long scriptsLocked; // for mint scripts we don't have scripts locked value

    // total number of transactions for dApp since beginning
    @DatabaseField(columnName = "trx_count", index = true)
    @Nullable // we don't always have contract calls, for instance for MINT scripts
    Long transactionsCount; // value across all scripts per dApp

    // total number of all script innovations belonging to this dApp
    @DatabaseField(canBeNull = false, columnName = "script_invocations", index = true)
    Long scriptInvocationsCount;

    @DatabaseField(canBeNull = false, columnName = "update_time", dataType = DataType.DATE_STRING)
    Date updateTime;

}
