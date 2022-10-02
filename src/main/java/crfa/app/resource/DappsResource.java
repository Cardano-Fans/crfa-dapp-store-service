package crfa.app.resource;

import crfa.app.domain.DappAggrType;
import crfa.app.domain.SortBy;
import crfa.app.domain.SortOrder;
import crfa.app.repository.epoch.DappsEpochRepository;
import crfa.app.repository.total.DappsRepository;
import crfa.app.resource.model.DappResult;
import crfa.app.service.DappService;
import io.micronaut.http.annotation.Controller;
import io.micronaut.http.annotation.Get;
import io.micronaut.http.annotation.QueryValue;
import jakarta.inject.Inject;
import lombok.extern.slf4j.Slf4j;
import lombok.val;

import java.util.List;
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
    public Optional<DappResult> findDappById(String id, @QueryValue Optional<DappAggrType> dappAggrType) {
        val dappAggrTypeWithFallback = dappAggrType.orElse(DappAggrType.def());

        return dappsRepository.findById(id)
                .map(dapp -> {
                    val scriptInvocationsCount = dappAggrTypeWithFallback == LAST ? dapp.getLastVersionScriptInvocationsCount() : dapp.getScriptInvocationsCount();
                    val scriptsLocked = dappAggrTypeWithFallback == LAST ? dapp.getLastVersionScriptsLocked() : dapp.getScriptsLocked();
                    val volume = dappAggrTypeWithFallback == LAST ? dapp.getLastVersionVolume() : dapp.getVolume();
                    val uniqueAccounts = dappAggrTypeWithFallback == LAST ? dapp.getLastVersionUniqueAccounts() : dapp.getUniqueAccounts();

                    val dappLevelEpochData = dappService.gatherEpochLevelData(dappsEpochRepository.findByDappId(id));

                    return DappResult.builder()
                            .category(dapp.getCategory())
                            .subCategory(dapp.getSubCategory())
                            .dAppType(dapp.getDAppType())
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
                            .epochData(dappLevelEpochData)
                            .lastClosedEpochsDelta(dappService.getLastClosedEpochsDelta(dappLevelEpochData).orElse(null))
                            .build();
                });
    }

                                      @Get(uri = "/list-dapps", produces = "application/json")
    public List<DappResult> listDapps(@QueryValue Optional<SortBy> sortBy,
                                      @QueryValue Optional<SortOrder> sortOrder,
                                      @QueryValue Optional<DappAggrType> dappAggrType) throws InvalidParameterException {
        val dappAggrTypeWithFallback = dappAggrType.orElse(DappAggrType.def());

        return dappsRepository.listDapps(sortBy, sortOrder, dappAggrTypeWithFallback)
                .stream().map(dapp -> {
                    val scriptInvocationsCount = dappAggrTypeWithFallback == LAST ? dapp.getLastVersionScriptInvocationsCount() : dapp.getScriptInvocationsCount();
                    val scriptsLocked = dappAggrTypeWithFallback == LAST ? dapp.getLastVersionScriptsLocked() : dapp.getScriptsLocked();
                    val volume = dappAggrTypeWithFallback == LAST ? dapp.getLastVersionVolume() : dapp.getVolume();
                    val uniqueAccounts = dappAggrTypeWithFallback == LAST ? dapp.getLastVersionUniqueAccounts() : dapp.getUniqueAccounts();

                    return DappResult.builder()
                            .category(dapp.getCategory())
                            .subCategory(dapp.getSubCategory())
                            .dAppType(dapp.getDAppType())
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
                            .build();
                }).toList();
    }

}
