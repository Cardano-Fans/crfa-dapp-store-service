package crfa.app.service.processor.epoch;

import crfa.app.client.metadata.DappReleaseItem;
import crfa.app.client.metadata.DappSearchItem;
import crfa.app.domain.*;
import crfa.app.repository.epoch.DappReleaseEpochRepository;
import crfa.app.service.DappService;
import crfa.app.service.processor.FeedProcessor;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import lombok.extern.slf4j.Slf4j;
import lombok.val;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.Optional;

import static crfa.app.domain.EraName.ALONZO;
import static crfa.app.domain.Purpose.MINT;
import static crfa.app.domain.Purpose.SPEND;
import static crfa.app.service.processor.epoch.ProcessorHelper.*;

@Slf4j
@Singleton
// DappReleasesFeedEpochProcessor handles medium level list-releases case
public class DappReleasesFeedEpochProcessor implements FeedProcessor {

    @Inject
    private DappReleaseEpochRepository dappScriptsEpochRepository;

    @Inject
    private DappService dappService;

    @Override
    public boolean isEpochProcessor() {
        return true;
    }

    @Override
    public void process(DappFeed dappFeed, InjestionMode injestionMode, FeedProcessingContext context) {
        val dappReleases = new ArrayList<DAppReleaseEpoch>();

        dappFeed.getDappSearchResult().forEach(dappSearchItem -> {
            dappSearchItem.getReleases().forEach(dappReleaseItem -> {
                val injestCurrentEpochOnly = injestionMode == InjestionMode.CURRENT_EPOCH_AND_AGGREGATES;

                val currentEpochNo = dappService.currentEpoch();

                if (injestCurrentEpochOnly) {
                    val dappItemEpoch = createDappItemEpoch(dappFeed, false, dappSearchItem, dappReleaseItem, currentEpochNo);
                    dappReleases.add(dappItemEpoch);
                    return;
                }

                for (val epochNo : Eras.epochsBetween(ALONZO, currentEpochNo)) {
                    val isClosedEpoch = epochNo < currentEpochNo;
                    val dappItemEpoch = createDappItemEpoch(dappFeed, isClosedEpoch, dappSearchItem, dappReleaseItem, epochNo);
                    dappReleases.add(dappItemEpoch);
                }
            });
        });

        log.info("Upserting, dapp releases epoch, count:{}", dappReleases.size());
        dappReleases.forEach(dappRelease -> dappScriptsEpochRepository.upsertDAppRelease(dappRelease));
        log.info("Upserted, dapp releases epoch.");

        dappScriptsEpochRepository.removeAllExcept(dappReleases);
    }

    private DAppReleaseEpoch createDappItemEpoch(DappFeed dappFeed,
                                            boolean isClosedEpoch,
                                            DappSearchItem dappSearchItem,
                                            DappReleaseItem dappReleaseItem,
                                            int epochNo) {

        val dappReleaseEpoch = new DAppReleaseEpoch();

        val key = String.format("%s.%.1f", dappSearchItem.getId(), dappReleaseItem.getReleaseNumber());

        dappReleaseEpoch.setId(String.format("%s.%d", key, epochNo));
        dappReleaseEpoch.setKey(key);

        dappReleaseEpoch.setDappId(dappSearchItem.getId());
        dappReleaseEpoch.setEpochNo(epochNo);
        dappReleaseEpoch.setName(dappSearchItem.getName());
        dappReleaseEpoch.setLink(dappSearchItem.getUrl());
        dappReleaseEpoch.setIcon(dappSearchItem.getIcon());
        dappReleaseEpoch.setCategory(dappSearchItem.getCategory());
        dappReleaseEpoch.setSubCategory(dappSearchItem.getSubCategory());
        dappReleaseEpoch.setUpdateTime(new Date());
        dappReleaseEpoch.setDAppType(DAppType.valueOf(dappSearchItem.getType()));
        dappReleaseEpoch.setTwitter(dappSearchItem.getTwitter());
        dappReleaseEpoch.setClosedEpoch(isClosedEpoch);

        dappReleaseEpoch.setReleaseNumber(dappReleaseItem.getReleaseNumber());
        dappReleaseEpoch.setReleaseName(dappReleaseItem.getReleaseName());
        dappReleaseEpoch.setFullName(String.format("%s - %s", dappSearchItem.getName(), dappReleaseItem.getReleaseName()));

        Optional.ofNullable(dappReleaseItem.getContract()).ifPresent(contract -> {
            dappReleaseEpoch.setContractOpenSource(contract.getOpenSource());
            dappReleaseEpoch.setContractLink(contract.getContractLink());
        });

        Optional.ofNullable(dappReleaseItem.getAudit()).ifPresent(audit -> {
            dappReleaseEpoch.setAuditLink(audit.getAuditLink());
            dappReleaseEpoch.setAuditor(audit.getAuditor());
        });

        var inflowsOutflows = 0L;
        var totalInvocations = 0L;
        var volume = 0L;
        var fees = 0L;
        var uniqueAccounts = new HashSet<String>();

        for (val scriptItem : dappReleaseItem.getScripts()) {
            val hash = scriptItem.getUnifiedHash();

            totalInvocations += loadInvocations(dappFeed, hash, epochNo);

            if (scriptItem.getPurpose() == SPEND) {
                inflowsOutflows += loadAdaBalance(dappFeed, hash, epochNo);
                volume += loadVolume(dappFeed, hash, epochNo);
                fees += loadFees(dappFeed, hash, epochNo);
                uniqueAccounts.addAll(loadUniqueAccounts(dappFeed, hash, epochNo));
            }

            if (scriptItem.getPurpose() == MINT && scriptItem.getAssetId().isPresent()) {
                inflowsOutflows += loadTokensBalance(dappFeed, scriptItem.getAssetId().orElseThrow(), epochNo);
            }
        }

        dappReleaseEpoch.setScriptInvocationsCount(totalInvocations);
        dappReleaseEpoch.setInflowsOutflows(inflowsOutflows);
        dappReleaseEpoch.setUniqueAccounts(uniqueAccounts.size());
        dappReleaseEpoch.setVolume(volume);
        dappReleaseEpoch.setFees(fees);

        return dappReleaseEpoch;
    }

}
