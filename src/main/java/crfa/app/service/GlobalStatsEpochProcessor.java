package crfa.app.service;

import crfa.app.domain.*;
import crfa.app.repository.GlobalStatsEpochRepository;
import crfa.app.repository.epoch.DappsEpochRepository;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import lombok.extern.slf4j.Slf4j;
import lombok.val;

import java.util.Date;
import java.util.Optional;

import static crfa.app.domain.EraName.ALONZO;

@Singleton
@Slf4j
public class GlobalStatsEpochProcessor {

    @Inject
    private GlobalStatsEpochRepository globalStatsEpochRepository;

    @Inject
    private DappsEpochRepository dappsEpochRepository;

    @Inject
    private DappService dappService;

    public void process(DappFeed dappFeed, InjestionMode injestionMode, FeedProcessingContext context) {
        val currentEpochNo = dappService.currentEpoch();

        val epochs = Eras.epochsBetween(ALONZO, currentEpochNo);

        val injestCurrentEpochOnly = injestionMode == InjestionMode.CURRENT_EPOCH_AND_AGGREGATES;

        if (injestCurrentEpochOnly) {
            val b = GlobalStatsEpoch.builder();
            b.epochNo(currentEpochNo);
            b.updateTime(new Date());

            b.inflowsOutflows(dappsEpochRepository.inflowsOutflows(currentEpochNo));
            b.totalTrxCount(dappsEpochRepository.totalScriptInvocations(currentEpochNo));
            b.totalVolume(dappsEpochRepository.volume(currentEpochNo));

            Optional.ofNullable(context.getUniqueAccountsEpoch().get(currentEpochNo)).ifPresent(uniqueAccounts -> {
                b.totalUniqueAccounts(uniqueAccounts.size());
            });

            globalStatsEpochRepository.upsert(b.build());

            return;
        }

        for (val epochNo : epochs) {
            val b = GlobalStatsEpoch.builder();
            b.epochNo(epochNo);
            b.updateTime(new Date());

            b.inflowsOutflows(dappsEpochRepository.inflowsOutflows(epochNo));
            b.totalTrxCount(dappsEpochRepository.totalScriptInvocations(epochNo));
            b.totalVolume(dappsEpochRepository.volume(epochNo));

            Optional.ofNullable(context.getUniqueAccountsEpoch().get(epochNo)).ifPresent(uniqueAccounts -> {
                b.totalUniqueAccounts(uniqueAccounts.size());
            });

            globalStatsEpochRepository.upsert(b.build());
        }
    }

}
