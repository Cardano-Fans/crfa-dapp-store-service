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
public class DappResult {

    String id; // e.g, CWpU1DBj

    String name; // e.g, Meld

    String fullName;

    DAppType dAppType;

    String link; // project link

    String icon;

    String twitter; // twitter link

    String category; // e.g. DEFI

    String subCategory; // e.g. LENDING_DAPP

    @Nullable
    Long scriptsLocked;

    @Nullable
    Long volume;

    @Nullable
    Long fees;

    @Nullable
    Integer uniqueAccounts;

    // unified for transactionsCount and scriptInvocationsCount
    Long trxCount;

    @Nullable
    String lastVersionContractsOpenSourcedLink;

    @Nullable
    String lastVersionContractsAuditedLink;

    Date updateTime;

    Optional<EpochLevelData> epochLevelData;

}
