package crfa.app.service;

import crfa.app.client.metadata.ScriptItem;
import crfa.app.domain.DAppRelease;
import crfa.app.domain.DAppType;
import crfa.app.domain.DappFeed;
import crfa.app.domain.Purpose;
import crfa.app.repository.DappReleasesRepository;
import io.micronaut.context.annotation.Value;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import lombok.extern.slf4j.Slf4j;
import lombok.val;

import java.util.Date;
import java.util.Optional;

@Slf4j
@Singleton
// DappReleasesFeedProcessor handles medium level list-releases case
public class DappReleasesFeedProcessor implements FeedProcessor {

    @Value("${dryRunMode:true}")
    private boolean dryRunMode;

    @Inject
    private DappReleasesRepository dappReleasesRepository;

    @Override
    public void process(DappFeed dappFeed) {
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

                dappRelease.setKey(String.format("%s.%f", dappSearchItem.getId(), dappReleaseItem.getReleaseNumber()));
                dappRelease.setReleaseNumber(dappReleaseItem.getReleaseNumber());
                dappRelease.setReleaseName(dappReleaseItem.getReleaseName());
                dappRelease.setFullName(String.format("%s - %s", dappSearchItem.getName(), dappReleaseItem.getReleaseName()));

                var totalScriptsLocked = 0L;
                var totalInvocations = 0L;
                var totalTransactionsCount = 0L;

                Optional.ofNullable(dappReleaseItem.getContract()).ifPresent(contract -> {
                    dappRelease.setContractOpenSource(contract.getOpenSource());
                    dappRelease.setContractLink(contract.getContractLink());
                });

                Optional.ofNullable(dappReleaseItem.getAudit()).ifPresent(audit -> {
                    dappRelease.setAuditLink(audit.getAuditLink());
                    dappRelease.setAuditor(audit.getAuditor());
                    // todo audit type
                });

                for (ScriptItem scriptItem : dappReleaseItem.getScripts()) {
                    val contractAddress = scriptItem.getContractAddress();

                    Long invocationsPerHash = null;
                    if (scriptItem.getPurpose() == Purpose.SPEND) {
                        val scriptHash = scriptItem.getScriptHash();

                        invocationsPerHash = dappFeed.getInvocationsCountPerHash().get(scriptHash);
                        if (invocationsPerHash == null) {
                            log.warn("Unable to find total invocations for scriptHash:{}, id:{}", scriptHash, scriptItem.getId());
                        }
                    }
                    if (scriptItem.getPurpose() == Purpose.MINT) {
                        val mintPolicyID = scriptItem.getMintPolicyID();
                        invocationsPerHash = dappFeed.getInvocationsCountPerHash().get(mintPolicyID);

                        if (invocationsPerHash == null) {
                            log.warn("Unable to find total invocations for mintPolicyID:{}, id:{}", mintPolicyID, scriptItem.getId());
                        }
                    }

                    if (invocationsPerHash != null) {
                        totalInvocations += invocationsPerHash;
                    }
                    if (contractAddress != null && scriptItem.getPurpose() == Purpose.SPEND) {
                        val scriptsLocked = dappFeed.getScriptLockedPerContractAddress().get(contractAddress);
                        if (scriptsLocked != null) {
                            totalScriptsLocked += scriptsLocked;
                        } else {
                            log.warn("Unable to find scriptsLocked for contractAddress:{}", contractAddress);
                        }

                        val trxCount = dappFeed.getTransactionCountsPerContractAddress().get(contractAddress);
                        if (trxCount != null) {
                            totalTransactionsCount += trxCount;
                        }
                    }
                    if (dappFeed.getTokenHoldersBalance() != null && scriptItem.getPurpose() == Purpose.MINT && scriptItem.getAssetNameAsHex().isPresent()) {
                        val assetNameHex = scriptItem.getAssetNameAsHex().get();

                        val adaBalance = dappFeed.getTokenHoldersBalance().get(assetNameHex);
                        if (adaBalance != null) {
                            log.info("Setting ada balance:{}, for mintPolicyId:{}", adaBalance, scriptItem.getMintPolicyID());
                            totalScriptsLocked += adaBalance;
                        }
                    }
                }

                dappRelease.setScriptInvocationsCount(totalInvocations);
                dappRelease.setScriptsLocked(totalScriptsLocked);
                dappRelease.setTransactionsCount(totalTransactionsCount);

                if (!dryRunMode) {
                    log.debug("Upserting, dappname:{} - {}", dappRelease.getName(), dappReleaseItem.getReleaseName());

                    dappReleasesRepository.upsertDAppRelease(dappRelease);
                }
            });
        });
    }

}
