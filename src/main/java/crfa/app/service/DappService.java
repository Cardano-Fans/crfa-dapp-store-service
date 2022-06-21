package crfa.app.service;

import crfa.app.client.metadata.CRFAMetaDataServiceClient;
import crfa.app.client.metadata.ScriptItem;
import crfa.app.domain.*;
import crfa.app.repository.DappReleaseItemRepository;
import crfa.app.repository.DappReleasesRepository;
import io.micronaut.context.annotation.Value;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RedissonClient;
import reactor.core.publisher.Mono;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Optional;

import static java.lang.Integer.MAX_VALUE;

@Singleton
@Slf4j
public class DappService {

    @Inject
    private DappReleasesRepository dappReleasesRepository;

    @Inject
    private DappReleaseItemRepository dappReleaseItemRepository;

    @Inject
    private CRFAMetaDataServiceClient crfaMetaDataServiceClient;

    @Inject
    private DbSyncService dbSyncService;

    @Inject
    private RedissonClient redissonClient;

    @Value("${dryRunMode:true}")
    private boolean dryRunMode;

    public DappFeed gatherDappDataFeed() {
        var dappSearchResult = Mono.from(crfaMetaDataServiceClient.fetchAllDapps()).block();

        var dappReleaseIdAddressPointersMap = new HashMap<DappReleaseId, AddressPointers>();

        dappSearchResult.forEach(dappSearchItem -> {

            dappSearchItem.getReleases().forEach(dappReleaseItem -> {
                var dappReleaseId = new DappReleaseId();
                dappReleaseId.setDappId(dappSearchItem.getId());
                dappReleaseId.setReleaseNumber(dappReleaseItem.getReleaseNumber());

                dappReleaseItem.getScripts().forEach(scriptItem -> {
                    if (scriptItem.getPurpose() == Purpose.MINT) {
                        dappReleaseId.setHash(scriptItem.getMintPolicyID());
                    } else if (scriptItem.getPurpose() == Purpose.SPEND) {
                        dappReleaseId.setHash(scriptItem.getScriptHash());
                    }

                    var addressPointer = new AddressPointers();
                    addressPointer.setScriptHash(scriptItem.getScriptHash());

                    if (scriptItem.getPurpose() == Purpose.SPEND) {
                        addressPointer.setContractAddress(scriptItem.getContractAddress());
                    }

                    dappReleaseIdAddressPointersMap.put(dappReleaseId, addressPointer);
                });
            });

        });

        var invocationsCountPerScriptHash = dbSyncService.topScripts(MAX_VALUE);

        var contractAddresses = dappReleaseIdAddressPointersMap.values().stream()
                .filter(addressPointers -> addressPointers.getContractAddress() != null)
                .map(AddressPointers::getContractAddress)
                .toList();

        log.info("Loading locked per contract address....");
        var scriptLockedPerContract = dbSyncService.scriptLocked(contractAddresses);
        log.info("Loaded locked per contract addresses.");

        log.info("Loading transaction counts....");
        var trxCounts = dbSyncService.transactionsCount(contractAddresses);
        log.info("Loaded trx counts.");

        return DappFeed.builder()
                .dappSearchResult(dappSearchResult)
                .scriptLockedPerContractAddress(scriptLockedPerContract)
                .invocationsCountPerHash(invocationsCountPerScriptHash)
                .transactionCountsPerContractAddress(trxCounts)
                .build();
    }

    public void upsertDappReleaseItems(DappFeed dappFeed) {
        var items = new ArrayList<DAppReleaseItem>();

        dappFeed.getDappSearchResult().forEach(dappSearchItem -> {

            dappSearchItem.getReleases().forEach(dappReleaseItem -> {
                for (ScriptItem scriptItem : dappReleaseItem.getScripts()) {
                    var newDappReleaseItem = new DAppReleaseItem();
                    newDappReleaseItem.setName(scriptItem.getName());
                    newDappReleaseItem.setReleaseKey(String.format("%s.%f", dappSearchItem.getId(), dappReleaseItem.getReleaseNumber()));
                    newDappReleaseItem.setVersion(scriptItem.getVersion());
                    newDappReleaseItem.setUpdateTime(new Date());

                    Long invocationsPerHash = null;
                    if (scriptItem.getPurpose() == Purpose.SPEND) {
                        var scriptHash = scriptItem.getScriptHash();

                        invocationsPerHash = dappFeed.getInvocationsCountPerHash().get(scriptHash);
                        if (invocationsPerHash == null) {
                            log.warn("Unable to find total invocations for scriptHash:{}, id:{}", scriptHash, scriptItem.getId());
                        }
                        newDappReleaseItem.setScriptType(ScriptType.SPEND);
                        newDappReleaseItem.setHash(scriptItem.getScriptHash());
                        newDappReleaseItem.setContractAddress(scriptItem.getContractAddress());
                    }
                    if (scriptItem.getPurpose() == Purpose.MINT) {
                        var mintPolicyID = scriptItem.getMintPolicyID();
                        invocationsPerHash = dappFeed.getInvocationsCountPerHash().get(mintPolicyID);

                        if (invocationsPerHash == null) {
                            log.warn("Unable to find total invocations for mintPolicyID:{}, id:{}", mintPolicyID, scriptItem.getId());
                        }

                        newDappReleaseItem.setScriptType(ScriptType.MINT);
                        newDappReleaseItem.setHash(scriptItem.getMintPolicyID());
                        newDappReleaseItem.setMintPolicyID(scriptItem.getMintPolicyID());
                    }

                    if (invocationsPerHash != null) {
                        newDappReleaseItem.setScriptInvocationsCount(invocationsPerHash);
                    } else {
                        newDappReleaseItem.setScriptInvocationsCount(0L);
                    }
                    var contractAddress = scriptItem.getContractAddress();
                    if (contractAddress != null && scriptItem.getPurpose() == Purpose.SPEND) {
                        var scriptsLocked = dappFeed.getScriptLockedPerContractAddress().get(contractAddress);
                        if (scriptsLocked != null) {
                            newDappReleaseItem.setScriptsLocked(scriptsLocked);
                        } else {
                            log.warn("Unable to find scriptsLocked for contractAddress:{}", contractAddress);
                        }

                        var trxCount = dappFeed.getTransactionCountsPerContractAddress().get(contractAddress);
                        if (trxCount != null) {
                            newDappReleaseItem.setTransactionsCount(trxCount);
                        }
                    }

                    items.add(newDappReleaseItem);
                }

                if (!dryRunMode) {
                    items.forEach(dAppReleaseItem -> {
                        log.info("Upserting, dapp item:{} - {}", dAppReleaseItem.getName(), dappReleaseItem.getReleaseName());
                        dappReleaseItemRepository.updatedAppReleaseItem(dAppReleaseItem);
                    });
                }
            });
        });
    }

    public void upsertDappReleases(DappFeed dappFeed) {
        dappFeed.getDappSearchResult().forEach(dappSearchItem -> {
            var dappRelease = new DAppRelease();

            dappRelease.setId(dappSearchItem.getId());
            dappRelease.setName(dappSearchItem.getName());
            dappRelease.setLink(dappSearchItem.getUrl());
            dappRelease.setIcon(dappSearchItem.getIcon());
            dappRelease.setCategory(dappSearchItem.getCategory());
            dappRelease.setSubCategory(dappSearchItem.getSubCategory());
            dappRelease.setUpdateTime(new Date());
            dappRelease.setDAppType(DAppType.valueOf(dappSearchItem.getType()));
            dappRelease.setTwitter(dappSearchItem.getTwitter());

            dappSearchItem.getReleases().forEach(dappReleaseItem -> {
                dappRelease.setKey(String.format("%s.%f", dappSearchItem.getId(), dappReleaseItem.getReleaseNumber()));
                dappRelease.setReleaseNumber(dappReleaseItem.getReleaseNumber());
                dappRelease.setReleaseName(dappReleaseItem.getReleaseName());
                dappRelease.setFullName(String.format("%s - %s", dappSearchItem.getName(), dappReleaseItem.getReleaseName()));

                var totalScriptsLocked = 0L;
                var totalInvocations = 0L;
                var totalTransactionsCount = 0L;

                for (ScriptItem scriptItem : dappReleaseItem.getScripts()) {
                    var contractAddress = scriptItem.getContractAddress();

                    Optional.ofNullable(scriptItem.getAudit()).ifPresent(audit -> {
                        dappRelease.setAuditLink(audit.getAuditLink());
                        dappRelease.setAuditor(audit.getAuditor());
                        // todo audit type
                    });

                    Optional.ofNullable(scriptItem.getContract()).ifPresent(contract -> {
                        dappRelease.setContractOpenSource(contract.getOpenSource());
                        dappRelease.setContractLink(contract.getContractLink());
                    });

                    Long invocationsPerHash = null;
                    if (scriptItem.getPurpose() == Purpose.SPEND) {
                        var scriptHash = scriptItem.getScriptHash();

                        invocationsPerHash = dappFeed.getInvocationsCountPerHash().get(scriptHash);
                        if (invocationsPerHash == null) {
                            log.warn("Unable to find total invocations for scriptHash:{}, id:{}", scriptHash, scriptItem.getId());
                        }
                    }
                    if (scriptItem.getPurpose() == Purpose.MINT) {
                        var mintPolicyID = scriptItem.getMintPolicyID();
                        invocationsPerHash = dappFeed.getInvocationsCountPerHash().get(mintPolicyID);

                        if (invocationsPerHash == null) {
                            log.warn("Unable to find total invocations for mintPolicyID:{}, id:{}", mintPolicyID, scriptItem.getId());
                        }
                    }

                    if (invocationsPerHash != null) {
                        totalInvocations += invocationsPerHash;
                    }
                    if (contractAddress != null && scriptItem.getPurpose() == Purpose.SPEND) {
                        var scriptsLocked = dappFeed.getScriptLockedPerContractAddress().get(contractAddress);
                        if (scriptsLocked != null) {
                            totalScriptsLocked += scriptsLocked;
                        } else {
                            log.warn("Unable to find scriptsLocked for contractAddress:{}", contractAddress);
                        }

//                        var transactionsCount = redissonClient.getAtomicLong(String.format("transactions_by_contract_address.%s", contractAddress));
                        var trxCount = dappFeed.getTransactionCountsPerContractAddress().get(contractAddress);
                        if (trxCount != null) {
                            totalTransactionsCount += trxCount;
                        }
                    }
                }

                dappRelease.setScriptInvocationsCount(totalInvocations);
                // TODO
                dappRelease.setTotalValueLocked(0L);
                dappRelease.setScriptsLocked(totalScriptsLocked);
                dappRelease.setTransactionsCount(totalTransactionsCount);

                if (!dryRunMode) {
                    log.info("Upserting, dappname:{} - {}", dappRelease.getName(), dappReleaseItem.getReleaseName());

                    dappReleasesRepository.upsertDAppRelease(dappRelease);
                }
            });
        });
    }

}
