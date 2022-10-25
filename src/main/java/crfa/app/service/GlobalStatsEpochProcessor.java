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
            globalStatsEpochRepository.upsert(createStats(context, currentEpochNo));
        } else {
            for (val epochNo : epochs) {
                globalStatsEpochRepository.upsert(createStats(context, epochNo));
            }
        }
    }

    private GlobalStatsEpoch createStats(FeedProcessingContext context, int epochNo) {
        val b = GlobalStatsEpoch.builder();
        b.epochNo(epochNo);
        b.updateTime(new Date());

        val spendTransactions = dappsEpochRepository.spendTransactions(epochNo);
        val mintTransactions = dappsEpochRepository.mintTransactions(epochNo);

        b.inflowsOutflows(dappsEpochRepository.inflowsOutflows(epochNo));
        b.spendTransactions(spendTransactions);
        b.mintTransactions(mintTransactions);
        b.spendVolume(dappsEpochRepository.spendVolume(epochNo));

        b.spendTrxSizes(dappsEpochRepository.spendTrxSizes(epochNo));
        b.spendTrxFees(dappsEpochRepository.spendFees(epochNo));
        b.transactions(mintTransactions + spendTransactions);

        Optional.ofNullable(context.getUniqueAccountsEpoch().get(epochNo)).ifPresent(uniqueAccounts -> {
            b.spendUniqueAccounts(uniqueAccounts.size());
        });

        return b.build();
    }

}
