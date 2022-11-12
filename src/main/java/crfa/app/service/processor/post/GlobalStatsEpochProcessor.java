package crfa.app.service.processor.post;

import crfa.app.domain.*;
import crfa.app.repository.GlobalStatsEpochRepository;
import crfa.app.repository.epoch.DappsEpochRepository;
import crfa.app.service.DappService;
import crfa.app.service.processor.FeedPostProcessor;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import lombok.extern.slf4j.Slf4j;
import lombok.val;

import java.util.Date;
import java.util.HashSet;

import static crfa.app.domain.EraName.MARY;
import static crfa.app.domain.InjestionMode.WITHOUT_EPOCHS_ONLY_AGGREGATES;
import static crfa.app.domain.Purpose.SPEND;
import static crfa.app.service.processor.epoch.ProcessorHelper.loadSpendUniqueAccounts;

@Singleton
@Slf4j
public class GlobalStatsEpochProcessor implements FeedPostProcessor {

    @Inject
    private GlobalStatsEpochRepository globalStatsEpochRepository;

    @Inject
    private DappsEpochRepository dappsEpochRepository;

    @Inject
    private DappService dappService;

    public void process(DappFeed dappFeed, InjestionMode injestionMode) {
        if (injestionMode == WITHOUT_EPOCHS_ONLY_AGGREGATES) {
            return;
        }

        val currentEpochNo = dappService.currentEpoch();

        val epochs = Eras.epochsBetween(SnapshotType.ALL.startEpoch(currentEpochNo), currentEpochNo);

        val injestCurrentEpochOnly = injestionMode == InjestionMode.CURRENT_EPOCH_AND_AGGREGATES;

        if (injestCurrentEpochOnly) {
            globalStatsEpochRepository.upsert(createStats(dappFeed, currentEpochNo));
        } else {
            for (val epochNo : epochs) {
                globalStatsEpochRepository.upsert(createStats(dappFeed, epochNo));
            }
        }
    }

    private GlobalStatsEpoch createStats(DappFeed dappFeed, int epochNo) {
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

        b.spendUniqueAccounts(uniqueAccounts(dappFeed, epochNo));

        return b.build();
    }

    private int uniqueAccounts(DappFeed dappFeed, int epochNo) {
        val spendUniqueAccounts = new HashSet<String>();

        for (val dsr : dappFeed.getDappSearchResult()) {
            for (val r : dsr.getReleases()) {
                for (val s : r.getScripts()) {
                    if (s.getPurpose() == SPEND) {
                        spendUniqueAccounts.addAll(loadSpendUniqueAccounts(dappFeed, s.getUnifiedHash(), epochNo));
                    }
                }
            }
        }

        return spendUniqueAccounts.size();
    }

}
