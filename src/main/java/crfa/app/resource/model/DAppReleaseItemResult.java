package crfa.app.resource.model;

import crfa.app.domain.ScriptType;
import io.micronaut.core.annotation.Nullable;
import lombok.Builder;
import lombok.Getter;

import java.util.Date;

@Builder
@Getter
public class DAppReleaseItemResult {

    String hash; // either script hash or mintPolicyId

    String dappId; // dapp id

    String releaseKey; // e.g, CWpU1DBj.V1 // foreign key to dapp_release

    ScriptType scriptType;

    @Nullable
    String name;

    int version;

    @Nullable
    String contractAddress;

    @Nullable
    String mintPolicyID;

    @Nullable
    Long scriptsLocked; // for mint scripts we don't have scripts locked value

    // total number of transactions for dApp since beginning
    @Nullable // we don't always have contract calls, for instance for MINT scripts
    @Deprecated
    Long transactionsCount; // value across all scripts per dApp

    // total number of all script innovations belonging to this dApp
    @Deprecated
    Long scriptInvocationsCount;

    // unified for transactionsCount and scriptInvocationsCount
    Long trxCount;

    Date updateTime;

}
