package crfa.app.resource.model;

import crfa.app.domain.EpochLevelData;
import crfa.app.domain.ScriptType;
import io.micronaut.core.annotation.Nullable;
import lombok.Builder;
import lombok.Getter;

import java.util.Date;
import java.util.Optional;

@Builder
@Getter
public class DAppScriptItemResult {

    String hash; // either script hash or mintPolicyId

    String dappId; // dapp id

    String releaseKey; // e.g, CWpU1DBj.V1 // foreign key to dapp_release

    ScriptType scriptType;

    @Nullable
    String name;

    int version;

//    @Nullable
//    String contractAddress;

    @Nullable
    String mintPolicyID;

    @Nullable
    Long scriptsLocked; // for mint scripts we don't have scripts locked value

    // unified for transactionsCount and scriptInvocationsCount
    Long trxCount;

    Date updateTime;

    @Nullable
    Integer uniqueAccounts;

    @Nullable
    Long volume;

    @Nullable
    Long fees;

    int plutusVersion;

    Optional<EpochLevelData> epochLevelData;

}
