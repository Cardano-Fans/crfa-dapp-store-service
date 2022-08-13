package crfa.app.service;

import crfa.app.client.metadata.CRFAMetaDataServiceClient;
import crfa.app.client.metadata.ScriptItem;
import crfa.app.domain.*;
import crfa.app.repository.DappReleaseItemRepository;
import crfa.app.repository.DappReleasesRepository;
import crfa.app.repository.DappsRepository;
import io.micronaut.context.annotation.Value;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RedissonClient;
import reactor.core.publisher.Mono;

import java.util.*;
import java.util.stream.Collectors;

@Singleton
@Slf4j
public class DappIngestionService {


    @Inject
    private DappsRepository dappsRepository;

    @Inject
    private DappReleasesRepository dappReleasesRepository;

    @Inject
    private DappReleaseItemRepository dappReleaseItemRepository;

    @Inject
    private CRFAMetaDataServiceClient crfaMetaDataServiceClient;

    @Inject
    private ScrollsService scrollsService;

    @Inject
    private DbSyncService dbSyncService;

    @Inject
    private DappService dappService;

    @Inject
    private RedissonClient redissonClient;

    @Value("${dryRunMode:true}")
    private boolean dryRunMode;

    public DappFeed gatherDappDataFeed() {
        var dappSearchResult = Mono.from(crfaMetaDataServiceClient.fetchAllDapps()).block();

        var addressPointersList = new HashSet<AddressPointers>();
        var mintPolicyIds = new ArrayList<String>();
        var mintPolicyIdsToTokenHolders = new HashMap<String, List<String>>();

        dappSearchResult.forEach(dappSearchItem -> {

            dappSearchItem.getReleases().forEach(dappReleaseItem -> {
                var dappReleaseId = new DappReleaseId();
                dappReleaseId.setDappId(dappSearchItem.getId());
                dappReleaseId.setReleaseNumber(dappReleaseItem.getReleaseNumber());

                dappReleaseItem.getScripts().forEach(scriptItem -> {
                    if (scriptItem.getPurpose() == Purpose.MINT) {
                        dappReleaseId.setHash(scriptItem.getMintPolicyID());
                        mintPolicyIds.add(scriptItem.getMintPolicyID());
                        if (scriptItem.getTokenHolders() != null) {
                            mintPolicyIdsToTokenHolders.put(scriptItem.getMintPolicyID(), scriptItem.getTokenHolders());
                        }
                    } else if (scriptItem.getPurpose() == Purpose.SPEND) {
                        dappReleaseId.setHash(scriptItem.getScriptHash());
                    }

                    var addressPointer = new AddressPointers();
                    addressPointer.setScriptHash(scriptItem.getScriptHash());

                    if (scriptItem.getPurpose() == Purpose.SPEND) {
                        addressPointer.setContractAddress(scriptItem.getContractAddress());
                    }

                    addressPointersList.add(addressPointer);
                });
            });

        });

        var contractAddresses = addressPointersList.stream()
                .filter(addressPointers -> addressPointers.getContractAddress() != null)
                .map(AddressPointers::getContractAddress)
                .toList();

        var scriptHashes = addressPointersList.stream()
                .filter(addressPointers -> addressPointers.getScriptHash() != null)
                .map(AddressPointers::getScriptHash)
                .toList();

        var mintPolicyCounts = scrollsService.mintScriptsCount(mintPolicyIds);
        var scriptHashesCount = scrollsService.scriptHashesCount(scriptHashes);

        var invocationsCountPerScriptHash = new HashMap<String, Long>();
        invocationsCountPerScriptHash.putAll(mintPolicyCounts);
        invocationsCountPerScriptHash.putAll(scriptHashesCount);

        log.debug("Loading locked per contract address....");
        var scriptLockedPerContract = dbSyncService.scriptLocked(contractAddresses);
        log.debug("Loaded locked per contract addresses.");

        log.debug("Loading transaction counts....");
        var trxCounts = scrollsService.transactionsCount(contractAddresses);
        log.debug("Loaded trx counts.");

        // handling special case for WingRiders and when mintPolicyId has token holders, see: https://github.com/Cardano-Fans/crfa-offchain-data-registry/issues/80
        var tokenHoldersMintPolicyIdToAdaBalance = mintPolicyIdsToTokenHolders.entrySet().stream()
                .map(e -> {
                    var mintPolicyId = e.getKey();
                    var addresses = e.getValue();
                    var balanceMap = dbSyncService.scriptLocked(addresses);
                    var adaBalance = balanceMap.values().stream().reduce(0L, Long::sum);

                    return new AbstractMap.SimpleEntry<>(mintPolicyId, adaBalance);
                })
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        Map.Entry::getValue
                ));

        return DappFeed.builder()
                .dappSearchResult(dappSearchResult)
                .scriptLockedPerContractAddress(scriptLockedPerContract)
                .invocationsCountPerHash(invocationsCountPerScriptHash)
                .transactionCountsPerContractAddress(trxCounts)
                .tokenHoldersBalance(tokenHoldersMintPolicyIdToAdaBalance)
                .build();
    }

    // dapp release items, which in fact means dapp release scripts
    public void upsertDappReleaseItems(DappFeed dappFeed) {
        var items = new ArrayList<DAppReleaseItem>();

        dappFeed.getDappSearchResult().forEach(dappSearchItem -> {

            dappSearchItem.getReleases().forEach(dappReleaseItem -> {
                for (ScriptItem scriptItem : dappReleaseItem.getScripts()) {
                    var newDappReleaseItem = new DAppReleaseItem();
                    newDappReleaseItem.setName(scriptItem.getName());
                    newDappReleaseItem.setDappId(dappSearchItem.getId());
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
                        if (dappFeed.getTokenHoldersBalance() != null && dappFeed.getTokenHoldersBalance().get(mintPolicyID) != null) {
                            var adaBalance = dappFeed.getTokenHoldersBalance().get(mintPolicyID);
                            log.info("setting script balance for mintPolicyId:{}, ada balance:{}", mintPolicyID, adaBalance);
                            newDappReleaseItem.setScriptsLocked(adaBalance);
                        }
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
                        log.debug("Upserting, dapp item:{} - {}", dAppReleaseItem.getName(), dappReleaseItem.getReleaseName());
                        dappReleaseItemRepository.updatedAppReleaseItem(dAppReleaseItem);
                    });
                }
            });
        });
    }

    // dapp releases
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
                    var contractAddress = scriptItem.getContractAddress();

                    // deprecated to remove - backwards compatibility for now
                    if (dappReleaseItem.getAudit() == null) {
                        Optional.ofNullable(scriptItem.getAudit()).ifPresent(audit -> {
                            dappRelease.setAuditLink(audit.getAuditLink());
                            dappRelease.setAuditor(audit.getAuditor());
                            // todo audit type
                        });
                    }

                    // deprecated to remove - backwards compatibility for now
                    if (dappReleaseItem.getContract() == null) {
                        Optional.ofNullable(scriptItem.getContract()).ifPresent(contract -> {
                            dappRelease.setContractOpenSource(contract.getOpenSource());
                            dappRelease.setContractLink(contract.getContractLink());
                        });
                    }

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

                        var trxCount = dappFeed.getTransactionCountsPerContractAddress().get(contractAddress);
                        if (trxCount != null) {
                            totalTransactionsCount += trxCount;
                        }
                    }
                    if (dappFeed.getTokenHoldersBalance() != null && scriptItem.getPurpose() == Purpose.MINT) {
                        var adaBalance = dappFeed.getTokenHoldersBalance().get(scriptItem.getMintPolicyID());
                        if (adaBalance != null) {
                            log.info("Setting ada balance:{}, for mintPolicyId:{}", adaBalance, scriptItem.getMintPolicyID());
                            totalScriptsLocked += adaBalance;
                        }
                    }
                }

                dappRelease.setScriptInvocationsCount(totalInvocations);
                // TODO - TVL
                dappRelease.setTotalValueLocked(0L);
                dappRelease.setScriptsLocked(totalScriptsLocked);
                dappRelease.setTransactionsCount(totalTransactionsCount);

                if (!dryRunMode) {
                    log.debug("Upserting, dappname:{} - {}", dappRelease.getName(), dappReleaseItem.getReleaseName());

                    dappReleasesRepository.upsertDAppRelease(dappRelease);
                }
            });
        });
    }

    // fully aggregated dapp releases
    public void upsertDapps(DappFeed dappFeed) {
        dappFeed.getDappSearchResult().forEach(dappSearchItem -> {
            var dapp = new DApp();

            dapp.setId(dappSearchItem.getId());
            dapp.setName(dappSearchItem.getName());
            dapp.setLink(dappSearchItem.getUrl());
            dapp.setIcon(dappSearchItem.getIcon());
            dapp.setCategory(dappSearchItem.getCategory());
            dapp.setSubCategory(dappSearchItem.getSubCategory());
            dapp.setUpdateTime(new Date());
            dapp.setDAppType(DAppType.valueOf(dappSearchItem.getType()));
            dapp.setTwitter(dappSearchItem.getTwitter());

            var totalScriptsLocked = 0L;
            var totalInvocations = 0L;
            var totalTransactionsCount = 0L;

            var maxReleaseCache = dappService.buildMaxReleaseVersionCache();

            for (var dappReleaseItem : dappSearchItem.getReleases()) {
                for (ScriptItem scriptItem : dappReleaseItem.getScripts()) {
                    var contractAddress = scriptItem.getContractAddress();

                    var maxVersion = maxReleaseCache.getIfPresent(dapp.getId());
                    boolean isLastVersion = Float.compare(dappReleaseItem.getReleaseNumber(), maxVersion) == 0;

                    Optional.ofNullable(dappReleaseItem.getContract()).ifPresent(contract -> {
                        if (isLastVersion) {
                            dapp.setLastVersionOpenSourced(contract.getOpenSource());
                        }
                    });

                    Optional.ofNullable(dappReleaseItem.getAudit()).ifPresent(audit -> {
                        if (isLastVersion) {
                            dapp.setLastVersionAudited(audit.getAuditLink() != null);
                        }
                    });

                    // deprecated to remove - backwards compatibility for now
                    if (dapp.getLastVersionAudited() == null) {
                        Optional.ofNullable(scriptItem.getAudit()).ifPresent(audit -> {
                            if (isLastVersion) {
                                dapp.setLastVersionAudited(audit.getAuditLink() != null);
                            }
                        });
                    }

                    // deprecated to remove - backwards compatibility for now
                    if (dapp.getLastVersionOpenSourced() == null) {
                        Optional.ofNullable(scriptItem.getContract()).ifPresent(contract -> {
                            if (isLastVersion) {
                                dapp.setLastVersionOpenSourced(contract.getOpenSource());
                            }
                        });
                    }

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

                    if (dappFeed.getTokenHoldersBalance() != null && scriptItem.getPurpose() == Purpose.MINT) {
                        var adaBalance = dappFeed.getTokenHoldersBalance().get(scriptItem.getMintPolicyID());
                        if (adaBalance != null) {
                            log.info("Setting ada balance:{}, for mintPolicyId:{}", adaBalance, scriptItem.getMintPolicyID());
                            totalScriptsLocked += adaBalance;
                        }
                    }
                }

                dapp.setScriptInvocationsCount(totalInvocations);
                // TODO
                dapp.setTotalValueLocked(0L);
                dapp.setScriptsLocked(totalScriptsLocked);
                dapp.setTransactionsCount(totalTransactionsCount);

                if (!dryRunMode) {
                    log.debug("Upserting dapp, dappname:{}", dapp.getName());

                    dappsRepository.upsertDApp(dapp);
                }
            }
        });
    }

}