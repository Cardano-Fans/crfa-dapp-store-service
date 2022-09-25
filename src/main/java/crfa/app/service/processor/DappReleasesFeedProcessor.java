package crfa.app.service.processor;

import crfa.app.domain.*;
import crfa.app.repository.DappReleaseRepository;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import lombok.extern.slf4j.Slf4j;
import lombok.val;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.Optional;

import static crfa.app.service.processor.ProcessorHelper.*;

@Slf4j
@Singleton
// DappReleasesFeedProcessor handles medium level list-releases case
public class DappReleasesFeedProcessor implements FeedProcessor {

    @Inject
    private DappReleaseRepository dappReleaseRepository;

    @Override
    public void process(DappFeed dappFeed, InjestionMode injestionMode) {
        val dappReleases = new ArrayList<DAppRelease>();

        dappFeed.getDappSearchResult().forEach(dappSearchItem -> {
            dappSearchItem.getReleases().forEach(dappReleaseItem -> {
                val dappRelease = new DAppRelease();

                dappRelease.setId(dappSearchItem.getId());
                dappRelease.setName(dappSearchItem.getName());
                dappRelease.setLink(dappSearchItem.getUrl());
                dappRelease.setIcon(dappSearchItem.getIcon());
                dappRelease.setCategory(dappSearchItem.getCategory());
                dappRelease.setSubCategory(dappSearchItem.getSubCategory());
                dappRelease.setUpdateTime(new Date());
                dappRelease.setDAppType(DAppType.valueOf(dappSearchItem.getType()));
                dappRelease.setTwitter(dappSearchItem.getTwitter());

                dappRelease.setKey(String.format("%s.%.1f", dappSearchItem.getId(), dappReleaseItem.getReleaseNumber()));
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
                var totalTransactionsCount = 0L;
                var uniqueAccounts = new HashSet<String>();

                for (val scriptItem : dappReleaseItem.getScripts()) {
                    if (scriptItem.getPurpose() == Purpose.SPEND) {
                        val contractAddress = scriptItem.getContractAddress();

                        totalInvocations += loadInvocationsPerHash(dappFeed, scriptItem.getScriptHash());
                        totalScriptsLocked += loadAddressBalance(dappFeed, contractAddress);
                        totalTransactionsCount += loadTransactionsCount(dappFeed, contractAddress);
                        uniqueAccounts.addAll(loadUniqueAccounts(dappFeed, contractAddress));
                    }
                    if (scriptItem.getPurpose() == Purpose.MINT) {
                        totalInvocations += loadInvocationsPerHash(dappFeed, scriptItem.getMintPolicyID());

                        if (scriptItem.getAssetId().isPresent()) {
                            totalScriptsLocked += loadTokensBalance(dappFeed, scriptItem.getAssetId().get());
                        }
                    }
                }

                dappRelease.setScriptInvocationsCount(totalInvocations);
                dappRelease.setScriptsLocked(totalScriptsLocked);
                dappRelease.setTransactionsCount(totalTransactionsCount);
                dappRelease.setUniqueAccounts(uniqueAccounts.size());

                dappReleases.add(dappRelease);
            });
        });

        log.info("Upserting, dapp releases, count:{}", dappReleases.size());
        dappReleases.forEach(dappRelease -> {
            dappReleaseRepository.upsertDAppRelease(dappRelease);
        });
        log.info("Upserted, dapp releases.");

        dappReleaseRepository.removeAllExcept(dappReleases);
    }

}
