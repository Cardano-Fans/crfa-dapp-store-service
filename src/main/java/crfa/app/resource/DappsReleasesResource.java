package crfa.app.resource;

import crfa.app.domain.*;
import crfa.app.repository.epoch.DappReleaseEpochRepository;
import crfa.app.repository.total.DappReleaseRepository;
import crfa.app.resource.model.DappReleaseResult;
import crfa.app.service.DappService;
import io.micronaut.http.annotation.Controller;
import io.micronaut.http.annotation.Get;
import io.micronaut.http.annotation.QueryValue;
import jakarta.inject.Inject;
import lombok.extern.slf4j.Slf4j;
import lombok.val;

import java.util.List;
import java.util.Optional;

import static crfa.app.domain.SortBy.RELEASE_NUMBER;
import static crfa.app.domain.SortOrder.ASC;

@Controller("/dapps")
@Slf4j
public class DappsReleasesResource {

    @Inject
    private DappReleaseRepository dappReleaseRepository;

    @Inject
    private DappReleaseEpochRepository dappReleaseEpochRepository;

    @Inject
    private DappService dappService;

    @Get(uri = "/list-releases", produces = "application/json")
    public List<DappReleaseResult> listDappReleases(@QueryValue Optional<SortBy> sortBy,
                                                    @QueryValue Optional<SortOrder> sortOrder,
                                                    @QueryValue Optional<Integer> epochGap) throws InvalidParameterException {
        val eGap = epochGap.orElse(1);
        val releaseVersionsCache = dappService.buildMaxReleaseVersionCache();

        val currentEpoch = dappService.currentEpoch();
        val fromEpoch = currentEpoch - (eGap + 2);

        return dappReleaseRepository.listDappReleases(sortBy, sortOrder)
                .stream()
                .filter(dapp -> canGoBackThatFar(dapp.getId(), eGap))
                .map(dAppRelease -> {
                    val maxReleaseVersion = releaseVersionsCache.getIfPresent(dAppRelease.getDappId());

                    val isLastVersion = dAppRelease.isLatestVersion(maxReleaseVersion);

                    val dappType = dAppRelease.getDAppType();

                    return DappReleaseResult.builder()
                            .id(dAppRelease.getDappId())
                            .category(dAppRelease.getCategory())
                            .subCategory(dAppRelease.getSubCategory())
                            .dAppType(dappType)
                            .fullName(dAppRelease.getFullName())
                            .icon(dAppRelease.getIcon())
                            .key(dAppRelease.getId())
                            .link(dAppRelease.getLink())
                            .releaseName(dAppRelease.getReleaseName())
                            .name(dAppRelease.getName())
                            .twitter(dAppRelease.getTwitter())
                            .updateTime(dAppRelease.getUpdateTime())
                            .releaseNumber(dAppRelease.getReleaseNumber())
                            .scriptsLocked(dAppRelease.getScriptsLocked())
                            .transactionsCount(dAppRelease.getTransactionsCount())
                            .trxCount(dAppRelease.getScriptInvocationsCount())
                            .latestVersion(isLastVersion)
                            .uniqueAccounts(dAppRelease.getUniqueAccounts())
                            .volume(dAppRelease.getVolume())
                            .contractOpenSourcedLink(dAppRelease.getContractLink())
                            .contractsAuditedLink(dAppRelease.getAuditLink())
                            .lastClosedEpochsDelta(getEpochDelta(fromEpoch, dAppRelease, dappType, eGap))
                            .build();
                }).toList();
    }

    @Get(uri = "/find-release/{id}", produces = "application/json")
    public List<DappReleaseResult> findDappRelease(String id, @QueryValue Optional<Integer> epochGap) throws InvalidParameterException {
        return listDappReleases(Optional.of(RELEASE_NUMBER), Optional.of(ASC), epochGap)
                .stream()
                .filter(dappReleaseResult -> dappReleaseResult.getId().equalsIgnoreCase(id))
                .toList();
    }

    private Optional<EpochDelta> getEpochDelta(int fromEpoch, DAppRelease dAppRelease, DAppType dappType, int eGap) {
        if (dappType.hasSpend()) {
            val id = dAppRelease.getId();

            val epochGatherableCol = dappReleaseEpochRepository.findByReleaseKey(id, fromEpoch);

            val dappLevelEpochData = dappService.gatherEpochLevelData(epochGatherableCol);

            return dappService.getLastClosedEpochsDelta(dappLevelEpochData, eGap);
        }

        return Optional.empty();
    }

    // we may not have so many epochs to show such a large epoch gap
    private boolean canGoBackThatFar(String releaseKey, int eGap) {
        return dappReleaseEpochRepository.dappScriptsEpochsCount(releaseKey) > (eGap + 2);
    }

}
