package crfa.app.service.processor.total;

import crfa.app.domain.*;
import crfa.app.repository.PoolRepository;
import crfa.app.repository.total.DappReleaseRepository;
import crfa.app.service.processor.FeedProcessor;
import crfa.app.utils.Json;
import io.vavr.control.Either;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import lombok.extern.slf4j.Slf4j;
import lombok.val;

import java.util.*;

import static crfa.app.domain.Purpose.MINT;
import static crfa.app.domain.Purpose.SPEND;
import static crfa.app.service.processor.total.ProcessorHelper.*;

@Slf4j
@Singleton
// DappReleasesFeedProcessor handles medium level list-releases case
public class DappReleasesFeedProcessor implements FeedProcessor {

    @Inject
    private DappReleaseRepository dappReleaseRepository;

    @Inject
    private PoolRepository poolRepository;

    @Inject
    private Json json;

    @Override
    public void process(DappFeed dappFeed, InjestionMode injestionMode) {
        val dappReleases = new ArrayList<DAppRelease>();

        dappFeed.getDappSearchResult().forEach(dappSearchItem -> {
            dappSearchItem.getReleases().forEach(dappReleaseItem -> {
                val dappRelease = new DAppRelease();

                dappRelease.setDappId(dappSearchItem.getId());
                dappRelease.setName(dappSearchItem.getName());
                dappRelease.setLink(dappSearchItem.getUrl());
                dappRelease.setIcon(dappSearchItem.getIcon());
                dappRelease.setCategory(dappSearchItem.getCategory());
                dappRelease.setSubCategory(dappSearchItem.getSubCategory());
                dappRelease.setUpdateTime(new Date());
                dappRelease.setDAppType(DAppType.valueOf(dappSearchItem.getType()));
                dappRelease.setTwitter(dappSearchItem.getTwitter());

                dappRelease.setId(String.format("%s.%.1f", dappSearchItem.getId(), dappReleaseItem.getReleaseNumber()));
                dappRelease.setReleaseNumber(dappReleaseItem.getReleaseNumber());
                dappRelease.setReleaseName(dappReleaseItem.getReleaseName());
                dappRelease.setFullName(String.format("%s - %s", dappSearchItem.getName(), dappReleaseItem.getReleaseName()));

                Optional.ofNullable(dappReleaseItem.getContract()).ifPresent(contract -> {
                    dappRelease.setContractOpenSource(contract.getOpenSource());
                    dappRelease.setContractLink(contract.getContractLink());
                });

                Optional.ofNullable(dappReleaseItem.getAudit()).ifPresent(audit -> {
                    dappRelease.setAuditLink(audit.getAuditLink());
                    dappRelease.setAuditor(audit.getAuditor());
                });

                var balance = 0L;

                var spendTransactions = 0L;
                var spendVolume = 0L;
                var spendTrxFees = 0L;
                var spendTrxSizes = 0L;
                var spendUniqueAccounts = new HashSet<String>();

                var mintTransactions = 0L;

                var pools = new LinkedHashSet<Optional<Either<PoolError, String>>>();

                for (val scriptItem : dappReleaseItem.getScripts()) {
                    val hash = scriptItem.getUnifiedHash();

                    if (scriptItem.getPurpose() == SPEND) {
                        balance += loadBalance(dappFeed, hash);

                        spendVolume += loadSpendVolume(dappFeed, hash);
                        spendTrxFees += loadSpendTrxFee(dappFeed, hash);
                        spendTrxSizes += loadSpendTrxSize(dappFeed, hash);
                        spendTransactions += loadSpendTransactionsCount(dappFeed, hash);
                        spendUniqueAccounts.addAll(loadSpendUniqueAccounts(dappFeed, hash));
                        pools.add(loadPoolHex(dappFeed, hash));
                    }
                    if (scriptItem.getPurpose() == MINT) {
                        mintTransactions += loadMintTransactionsCount(dappFeed, hash);

                        if (scriptItem.getAssetId().isPresent()) {
                            balance += loadTokensBalance(dappFeed, scriptItem.getAssetId().orElseThrow());
                        }
                    }
                }

                dappRelease.setMintTransactions(mintTransactions);

                dappRelease.setBalance(balance);
                dappRelease.setSpendTransactions(spendTransactions);
                dappRelease.setSpendUniqueAccounts(spendUniqueAccounts.size());
                dappRelease.setSpendVolume(spendVolume);
                dappRelease.setSpendTrxFees(spendTrxFees);
                dappRelease.setSpendTrxSizes(spendTrxSizes);
                dappRelease.setTransactions(mintTransactions + spendTransactions);

                dappReleases.add(dappRelease);
            });
        });

        log.info("Upserting, dapp releases, count:{}", dappReleases.size());
        dappReleases.forEach(dappRelease -> dappReleaseRepository.upsertDAppRelease(dappRelease));
        log.info("Upserted, dapp releases.");

        dappReleaseRepository.removeAllExcept(dappReleases);
    }

}
