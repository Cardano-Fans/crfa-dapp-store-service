package crfa.app.service;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import crfa.app.domain.*;
import crfa.app.repository.epoch.DappReleaseEpochRepository;
import crfa.app.repository.epoch.DappScriptsEpochRepository;
import crfa.app.repository.epoch.DappsEpochRepository;
import crfa.app.repository.total.DappReleaseRepository;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import lombok.val;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import static crfa.app.domain.SortBy.SCRIPTS_INVOKED;
import static crfa.app.domain.SortOrder.ASC;

@Singleton
public class DappService {

    @Inject
    private DappReleaseRepository dappReleaseRepository;

    @Inject
    private ScrollsOnChainDataService scrollsOnChainDataService;

    @Inject
    private DappsEpochRepository dappsEpochRepository;

    @Inject
    private DappReleaseEpochRepository dappReleaseEpochRepository;

    @Inject
    private DappScriptsEpochRepository dappScriptsEpochRepository;

    public Cache<String, Float> buildMaxReleaseVersionCache() {
        val releaseVersionsCache = CacheBuilder.newBuilder().<String, Float>build();

        dappReleaseRepository.listDappReleases(SCRIPTS_INVOKED, ASC).forEach(dAppRelease -> {
            val dappId = dAppRelease.getDappId();
            releaseVersionsCache.put(dappId, dappReleaseRepository.getMaxReleaseVersion(dappId));
        });

        return releaseVersionsCache;
    }
    public Map<Integer, EpochLevelStats> gatherEpochLevelStats(Collection<? extends EpochGatherable> it) {
        val epochLevelStats = new HashMap<Integer, EpochLevelStats>();

        it.forEach(epochGatherable -> {
            val epochNo = epochGatherable.getEpochNo();

            epochLevelStats.put(epochNo, EpochLevelStats.builder()
                    .startEpoch(epochNo)
                    .endEpoch(epochNo)
                    .volume(epochGatherable.getSpendVolume())
                    .spendTrxFees(epochGatherable.getSpendTrxFees())
                    .spendTrxSizes(epochGatherable.getSpendTrxSizes())
                    .inflowsOutflows(epochGatherable.getInflowsOutflows())
                    .uniqueAccounts(epochGatherable.getSpendUniqueAccounts())
                    .trxCount(epochGatherable.getTransactions())
                    .closed(epochGatherable.isClosedEpoch())
                    .build()
            );
        });

        return epochLevelStats;
    }

    public Optional<EpochLevelData> getAllEpochLevelData(DApp dapp) {
        val id = dapp.getId();
        var dappEpochList = dappsEpochRepository.findByDappId(id);
        val epochLevelStatsMap = gatherEpochLevelStats(dappEpochList);

        return getEpochLevelData(epochLevelStatsMap);
    }

    public Optional<EpochLevelData> getAllEpochLevelData(DAppRelease dAppRelease) {
        val releaseKey = dAppRelease.getId();
        val epochLevelStatsMap = gatherEpochLevelStats(dappReleaseEpochRepository.findByReleaseKey(releaseKey));

        return getEpochLevelData(epochLevelStatsMap);
    }

    private Optional<EpochLevelData> getEpochLevelData(Map<Integer, EpochLevelStats> epochData) {
        if (epochData.isEmpty()) {
            return Optional.empty();
        }

        val one = getLastClosedEpochsDelta(epochData, SnapshotType.ONE);
        val six = getLastClosedEpochsDelta(epochData, SnapshotType.SIX);
        val eighteen = getLastClosedEpochsDelta(epochData, SnapshotType.EIGHTEEN);

        if (one.isEmpty()) {
            return Optional.empty();
        }

        val b = EpochLevelData.builder()
                .epochData(Optional.empty())
                .lastEpochDeltaWithOnlyClosedEpochs(one)
                .lastMonthDeltaWithOnlyClosedEpochs(six)
                .lastQuarterDeltaWithOnlyClosedEpochs(eighteen);

        b.epochData(Optional.of(epochData));

        return Optional.of(b.build());
    }

    public Optional<EpochLevelData> getAllEpochLevelData(DappScriptItem dappScriptItem) {
        val hash = dappScriptItem.getHash();
        val dappScriptItemEpoches = dappScriptsEpochRepository.listByHash(hash);
        val epochData = gatherEpochLevelStats(dappScriptItemEpoches);

        return getEpochLevelData(epochData);
    }

    public Optional<EpochLevelDiff> getLastClosedEpochsDelta(Map<Integer, EpochLevelStats> stats,
                                                             SnapshotType snapshotType) {
        return accumulateStatsBetweenEpochs(stats, snapshotType)
                .flatMap(snapshot -> {
                    val from = stats.getOrDefault(snapshot.getStartEpoch(), EpochLevelStats.zero(snapshot.getStartEpoch(), snapshot.getEndEpoch()));
                    val to = stats.getOrDefault(snapshot.getEndEpoch(), EpochLevelStats.zero(snapshot.getStartEpoch(), snapshot.getEndEpoch()));

                    val epochLevel = EpochLevelDiff.builder()
                            .from(from)
                            .to(to)
                            .snapshot(snapshot)
                            .build();

                    return Optional.of(epochLevel);
                });
    }

    public Optional<EpochLevelStats> accumulateStatsBetweenEpochs(Map<Integer, EpochLevelStats> stats,
                                                       SnapshotType snapshotType) {
        val closedEpochsMap = stats
                .entrySet()
                .stream()
                .filter(entry -> entry.getValue().isClosed()).collect(Collectors.toMap(
                        Map.Entry::getKey,
                        Map.Entry::getValue
                ));

        var volume = 0L;
        var spendTrxFees = 0L;
        var spendTrxSizes = 0L;
        var inflowOutflows = 0L;
        var trxCount = 0L;

        val lastClosedEpoch = lastClosedEpoch(closedEpochsMap);

        if (lastClosedEpoch.isEmpty()) {
            return Optional.empty();
        }
        val lce = lastClosedEpoch.orElseThrow();

        val lse = snapshotType.startEpoch(lce);

        val epochs = Eras.epochsBetween(lse, lce);

        for (val epoch : epochs) {
            val epochStats = stats.getOrDefault(epoch, EpochLevelStats.zero(lse, lce));
            if (epochStats.getVolume() != null) {
                volume += epochStats.getVolume();
            }
            if (epochStats.getSpendTrxFees() != null) {
                spendTrxFees += epochStats.getSpendTrxFees();
            }
            if (epochStats.getSpendTrxSizes() != null) {
                spendTrxSizes += epochStats.getSpendTrxSizes();
            }
            if (epochStats.getInflowsOutflows() != null) {
                inflowOutflows += epochStats.getInflowsOutflows();
            }

            trxCount += epochStats.getTrxCount();
        }

        return Optional.of(EpochLevelStats.builder()
                .startEpoch(lse)
                .endEpoch(lce)
                .closed(true)
                .volume(volume)
                .spendTrxFees(spendTrxFees)
                .spendTrxSizes(spendTrxSizes)
                .inflowsOutflows(inflowOutflows)
                .trxCount(trxCount)
                .uniqueAccounts(0)
                .build());
    }

    public int currentEpoch() {
        return scrollsOnChainDataService.currentEpoch().orElseThrow();
    }

    public Optional<Integer> lastClosedEpoch(Map<Integer, EpochLevelStats> closedEpochsMap) {
        // find out max epoch which is closed
        return closedEpochsMap.keySet().stream().max(Integer::compare);
    }

}
