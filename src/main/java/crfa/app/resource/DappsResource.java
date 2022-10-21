package crfa.app.resource;

import crfa.app.domain.SortBy;
import crfa.app.domain.SortOrder;
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

import static crfa.app.utils.MoreMath.safeDivision;

@Controller("/dapps")
@Slf4j
public class DappsResource {

    @Inject
    private DappsRepository dappsRepository;

    @Inject
    private DappService dappService;

    @Get(uri = "/find-dapp/{id}", produces = "application/json")
    public Optional<DappResult> findDappById(String id) {

        return dappsRepository.findById(id)
                .map(dapp -> {
                    val scriptInvocationsCount = dapp.getScriptInvocationsCount();
                    val scriptsLocked = dapp.getScriptsLocked();
                    val volume = dapp.getVolume();
                    val fees = dapp.getFees();
                    val uniqueAccounts = dapp.getUniqueAccounts();

                    val dAppType = dapp.getDAppType();

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
                            .fees(fees)
                            .avgFee(safeDivision(fees, dapp.getScriptInvocationsCount()))
                            .uniqueAccounts(uniqueAccounts)
                            .lastVersionContractsOpenSourcedLink(dapp.getLastVersionOpenSourceLink())
                            .lastVersionContractsAuditedLink(dapp.getLastVersionAuditLink())
                            .trxCount(scriptInvocationsCount)
                            .updateTime(dapp.getUpdateTime())
                            .epochLevelData(dappService.getAllEpochLevelData(dapp, true))
                            .build();
                });
    }

                                      @Get(uri = "/list-dapps", produces = "application/json")
    public List<DappResult> listDapps(@QueryValue Optional<SortBy> sortBy,
                                      @QueryValue Optional<SortOrder> sortOrder) {
         return dappsRepository.listDapps(sortBy.orElse(SortBy.SCRIPTS_INVOKED), sortOrder.orElse(SortOrder.DESC))
                .stream()
                 .map(dapp -> {
                    val scriptInvocationsCount = dapp.getScriptInvocationsCount();
                    val scriptsLocked = dapp.getScriptsLocked();
                    val volume = dapp.getVolume();
                    val fees = dapp.getFees();
                    val uniqueAccounts = dapp.getUniqueAccounts();

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
                            .fees(fees)
                            .avgFee(safeDivision(fees, dapp.getScriptInvocationsCount()))
                            .uniqueAccounts(uniqueAccounts)
                            .lastVersionContractsOpenSourcedLink(dapp.getLastVersionOpenSourceLink())
                            .lastVersionContractsAuditedLink(dapp.getLastVersionAuditLink())
                            .trxCount(scriptInvocationsCount)
                            .updateTime(dapp.getUpdateTime())
                            .epochLevelData(dappService.getAllEpochLevelData(dapp, false))
                            .build();
                }).toList();
    }

}
