package crfa.app.resource.model;

import crfa.app.domain.DAppType;
import crfa.app.domain.EpochLevelData;
import io.micronaut.core.annotation.Nullable;
import lombok.*;

import java.util.Date;
import java.util.Optional;

@Builder
@Getter
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class DappReleaseResult {

    String key; // e.g, CWpU1DBj.V1

    String id; // e.g, CWpU1DBj

    String name; // e.g, Meld

    String fullName;

    float releaseNumber;

    String releaseName;

    DAppType dAppType;

    String link; // project link

    String icon;

    String twitter; // twitter link

    String category; // e.g. DEFI

    String subCategory; // e.g. LENDING_DAPP

    @Nullable
    Long scriptsLocked;

    @Nullable
    @Deprecated
    Long transactionsCount;

    // unified for transactionsCount and scriptInvocationsCount
    Long trxCount;

    @Nullable
    Long volume;

    @Nullable
    Long fees;

    @Nullable
    Double avgTrxFee;

    @Nullable
    Double avgTrxSize;

    Date updateTime;

    @Nullable
    Integer uniqueAccounts;

    @Nullable
    String contractOpenSourcedLink;

    @Nullable
    String contractsAuditedLink;

    boolean latestVersion;

    Optional<EpochLevelData> epochLevelData;

}
