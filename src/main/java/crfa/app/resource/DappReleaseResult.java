package crfa.app.resource;

import crfa.app.domain.DAppType;
import io.micronaut.core.annotation.Nullable;
import lombok.*;

import java.util.Date;

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

    @Nullable // we don't always have contract calls, for instance for MINT scripts
    Long transactionsCount; // value across all scripts per dApp

    Long scriptInvocationsCount;

    Date updateTime;

    @Nullable
    Boolean contractOpenSource;

    @Nullable
    Boolean contractAudited;

    boolean latestVersion;

}
