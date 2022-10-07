package crfa.app.resource;

import crfa.app.domain.*;
import crfa.app.repository.epoch.DappsEpochRepository;
import crfa.app.repository.total.DappsRepository;
import crfa.app.resource.model.DappResult;
import crfa.app.resource.model.EpochLevelStats;
import crfa.app.service.DappService;
import io.micronaut.http.annotation.Controller;
import io.micronaut.http.annotation.Get;
import io.micronaut.http.annotation.QueryValue;
import io.vavr.Tuple2;
import jakarta.inject.Inject;
import lombok.extern.slf4j.Slf4j;
import lombok.val;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import static crfa.app.domain.DappAggrType.LAST;

@Controller("/dapps")
@Slf4j
public class DappsResource {

    @Inject
    private DappsRepository dappsRepository;

    @Inject
    private DappsEpochRepository dappsEpochRepository;

    @Inject
    private DappService dappService;

    @Get(uri = "/find-dapp/{id}", produces = "application/json")
    public Optional<DappResult> findDappById(String id,
                                             @QueryValue Optional<DappAggrType> dappAggrType,
                                             @QueryValue Optional<Integer> epochGap) {
        val eGap = epochGap.orElse(1);
        val dappAggrTypeWithFallback = dappAggrType.orElse(DappAggrType.def());

        val currentEpoch = dappService.currentEpoch();
        val fromEpoch = currentEpoch - (eGap + 1);

        return dappsRepository.findById(id)
                .filter(dApp ->  canGoBackThatFar(dApp.getId(), eGap))
                .map(dapp -> {
                    val scriptInvocationsCount = dappAggrTypeWithFallback == LAST ? dapp.getLastVersionScriptInvocationsCount() : dapp.getScriptInvocationsCount();
                    val scriptsLocked = dappAggrTypeWithFallback == LAST ? dapp.getLastVersionScriptsLocked() : dapp.getScriptsLocked();
                    val volume = dappAggrTypeWithFallback == LAST ? dapp.getLastVersionVolume() : dapp.getVolume();
                    val uniqueAccounts = dappAggrTypeWithFallback == LAST ? dapp.getLastVersionUniqueAccounts() : dapp.getUniqueAccounts();

                    val dAppType = dapp.getDAppType();

                    val tuple = getEpochData(dapp, fromEpoch, eGap);

                    return DappResult.builder()
                            .category(dapp.getCategory())
                            .subCategory(dapp.getSubCategory())
                            .dAppType(dAppType)
                            .id(dapp.getId())
                            .icon(dapp.getIcon())
                            .link(dapp.getLink())
                            .name(dapp.getName())
                            .twitter(dapp.getTwitter())
                            .scriptsLocked(scriptsLocked)
                            .volume(volume)
                            .uniqueAccounts(uniqueAccounts)
                            .lastVersionContractsOpenSourcedLink(dapp.getLastVersionOpenSourceLink())
                            .lastVersionContractsAuditedLink(dapp.getLastVersionAuditLink())
                            .trxCount(scriptInvocationsCount)
                            .updateTime(dapp.getUpdateTime())
                            .epochData(tuple.map(t -> t._2).orElse(Map.of()))
                            .lastClosedEpochsDelta(tuple.map(t -> t._1))
                            .build();
                });
    }

                                      @Get(uri = "/list-dapps", produces = "application/json")
    public List<DappResult> listDapps(@QueryValue Optional<SortBy> sortBy,
                                      @QueryValue Optional<SortOrder> sortOrder,
                                      @QueryValue Optional<DappAggrType> dappAggrType,
                                      @QueryValue Optional<Integer> epochGap) throws InvalidParameterException {

        val eGap = epochGap.orElse(1);
        val dappAggrTypeWithFallback = dappAggrType.orElse(DappAggrType.def());

        val currentEpoch = dappService.currentEpoch();
        val fromEpoch = currentEpoch - (eGap + 1);

         return dappsRepository.listDapps(sortBy, sortOrder, dappAggrTypeWithFallback)
                .stream()
                 .filter(dApp -> canGoBackThatFar(dApp.getId(), eGap))
                 .map(dapp -> {
                    val scriptInvocationsCount = dappAggrTypeWithFallback == LAST ? dapp.getLastVersionScriptInvocationsCount() : dapp.getScriptInvocationsCount();
                    val scriptsLocked = dappAggrTypeWithFallback == LAST ? dapp.getLastVersionScriptsLocked() : dapp.getScriptsLocked();
                    val volume = dappAggrTypeWithFallback == LAST ? dapp.getLastVersionVolume() : dapp.getVolume();
                    val uniqueAccounts = dappAggrTypeWithFallback == LAST ? dapp.getLastVersionUniqueAccounts() : dapp.getUniqueAccounts();

                    return DappResult.builder()
                            .id(dapp.getId())
                            .category(dapp.getCategory())
                            .subCategory(dapp.getSubCategory())
                            .dAppType(dapp.getDAppType())
                            .icon(dapp.getIcon())
                            .link(dapp.getLink())
                            .name(dapp.getName())
                            .twitter(dapp.getTwitter())
                            .scriptsLocked(scriptsLocked)
                            .volume(volume)
                            .uniqueAccounts(uniqueAccounts)
                            .lastVersionContractsOpenSourcedLink(dapp.getLastVersionOpenSourceLink())
                            .lastVersionContractsAuditedLink(dapp.getLastVersionAuditLink())
                            .trxCount(scriptInvocationsCount)
                            .updateTime(dapp.getUpdateTime())
                            .lastClosedEpochsDelta(getEpochData(dapp, fromEpoch, eGap).map(t -> t._1))
                            .build();
                }).toList();
    }

    private Optional<Tuple2<EpochDelta, Map<Integer, EpochLevelStats>>> getEpochData(DApp dapp, int fromEpoch, int eGap) {
        if (dapp.getDAppType().hasSpend()) {
            val dappLevelEpochData = dappService.gatherEpochLevelData(dappsEpochRepository.findByDappId(dapp.getId(), fromEpoch));

            return dappService.getLastClosedEpochsDelta(dappLevelEpochData, eGap).map(delta -> new Tuple2<>(delta, dappLevelEpochData));
        }

        return Optional.empty();
    }

    // we may not have so many epochs to show such a large epoch gap
    private boolean canGoBackThatFar(String dappId, int eGap) {
        return dappsEpochRepository.dappEpochsCount(dappId) > (eGap + 1);
    }

}
