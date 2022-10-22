package crfa.app.service.processor.total;

import crfa.app.domain.*;
import crfa.app.repository.total.DappReleaseRepository;
import crfa.app.service.processor.FeedProcessor;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import lombok.extern.slf4j.Slf4j;
import lombok.val;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.Optional;

import static crfa.app.domain.Purpose.MINT;
import static crfa.app.domain.Purpose.SPEND;
import static crfa.app.service.processor.total.ProcessorHelper.*;
import static crfa.app.utils.MoreMath.safeDivision;

@Slf4j
@Singleton
// DappReleasesFeedProcessor handles medium level list-releases case
public class DappReleasesFeedProcessor implements FeedProcessor {

    @Inject
    private DappReleaseRepository dappReleaseRepository;

    @Override
    public void process(DappFeed dappFeed, InjestionMode injestionMode, FeedProcessingContext context) {
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

                var totalScriptsLocked = 0L;
                var totalInvocations = 0L;
                var volume = 0L;
                var fees = 0L;
                var trxSizes = 0L;
                var uniqueAccounts = new HashSet<String>();

                for (val scriptItem : dappReleaseItem.getScripts()) {
                    val hash = scriptItem.getUnifiedHash();

                    totalInvocations += loadInvocations(dappFeed, hash);

                    if (scriptItem.getPurpose() == SPEND) {
                        volume += loadVolume(dappFeed, hash);
                        fees += loadFee(dappFeed, hash);
                        totalScriptsLocked += loadAdaBalance(dappFeed, hash);
                        trxSizes += loadTrxSize(dappFeed, hash);
                        uniqueAccounts.addAll(loadUniqueAccounts(dappFeed, hash));
                    }
                    if (scriptItem.getPurpose() == MINT && scriptItem.getAssetId().isPresent()) {
                        totalScriptsLocked += loadTokensBalance(dappFeed, scriptItem.getAssetId().get());
                    }
                }

                dappRelease.setScriptInvocationsCount(totalInvocations);
                dappRelease.setScriptsLocked(totalScriptsLocked);
                dappRelease.setUniqueAccounts(uniqueAccounts.size());
                dappRelease.setVolume(volume);
                dappRelease.setFees(fees);
                dappRelease.setTrxSizes(trxSizes);

                dappReleases.add(dappRelease);
            });
        });

        log.info("Upserting, dapp releases, count:{}", dappReleases.size());
        dappReleases.forEach(dappRelease -> dappReleaseRepository.upsertDAppRelease(dappRelease));
        log.info("Upserted, dapp releases.");

        dappReleaseRepository.removeAllExcept(dappReleases);
    }

}
