package crfa.app.repository.epoch;

import com.j256.ormlite.stmt.QueryBuilder;
import crfa.app.domain.DApp;
import crfa.app.domain.DAppEpoch;
import crfa.app.repository.DbManager;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import lombok.extern.slf4j.Slf4j;
import lombok.val;

import java.sql.SQLException;
import java.util.Collection;
import java.util.List;

@Singleton
@Slf4j
public class DappsEpochRepository {

    @Inject
    private DbManager dbManager;

    public Long spendVolume() {
        val statementBuilder = dbManager.getdAppDao().queryBuilder();

        try {
            return statementBuilder.query()
                    .stream()
                    .filter(dApp -> dApp.getSpendVolume() != null)
                    .mapToLong(DApp::getSpendVolume)
                    .sum();
        } catch (SQLException e) {
            log.error("db error", e);
            throw new RuntimeException(e);
        }
    }

    public List<DAppEpoch> findByDappId(String dappId) {
        try {
            val statementBuilder = dbManager.getdAppEpochDao().queryBuilder();

            statementBuilder
                    .where().eq("dapp_id", dappId);

            return dbManager.getdAppEpochDao().query(statementBuilder.prepare());
        } catch (SQLException e) {
            log.error("db error", e);
            throw new RuntimeException(e);
        }
    }

    public long inflowsOutflows(int epochNo) {
        try {
            val statementBuilder = dbManager.getdAppEpochDao()
                    .queryBuilder();

            return statementBuilder
                    .where()
                    .eq("epoch_no", epochNo)
                    .query()
                    .stream()
                    .filter(dAppEpoch -> dAppEpoch.getInflowsOutflows() != null)
                    .mapToLong(dappEpoch -> dappEpoch.getInflowsOutflows())
                    .sum();
        } catch (SQLException e) {
            log.error("db error", e);
            throw new RuntimeException(e);
        }
    }

    public long spendTransactions(int epochNo) {
        try {
            val statementBuilder = dbManager.getdAppEpochDao()
                .queryBuilder();

            return statementBuilder
                .where()
                .eq("epoch_no", epochNo)
                .query()
                .stream()
                .filter(dAppEpoch -> dAppEpoch.getSpendTransactions() != null)
                .mapToLong(dappEpoch -> dappEpoch.getSpendTransactions())
                .sum();
        } catch (SQLException e) {
            log.error("db error", e);
            throw new RuntimeException(e);
        }
    }

    public long mintTransactions(int epochNo) {
        try {
            val statementBuilder = dbManager.getdAppEpochDao()
                    .queryBuilder();

            return statementBuilder
                    .where()
                    .eq("epoch_no", epochNo)
                    .query()
                    .stream()
                    .filter(dAppEpoch -> dAppEpoch.getMintTransactions() != null)
                    .mapToLong(dappEpoch -> dappEpoch.getMintTransactions())
                    .sum();
        } catch (SQLException e) {
            log.error("db error", e);
            throw new RuntimeException(e);
        }
    }

    public long spendVolume(int epochNo) {
        try {
            val statementBuilder = dbManager.getdAppEpochDao()
                    .queryBuilder();

            return statementBuilder
                    .where()
                    .eq("epoch_no", epochNo)
                    .query()
                    .stream()
                    .filter(dAppEpoch -> dAppEpoch.getSpendVolume() != null)
                    .mapToLong(dappEpoch -> dappEpoch.getSpendVolume())
                    .sum();
        } catch (SQLException e) {
            log.error("db error", e);
            throw new RuntimeException(e);
        }
    }

    public long spendFees(int epochNo) {
        try {
            val statementBuilder = dbManager.getdAppEpochDao()
                    .queryBuilder();

            return statementBuilder
                    .where()
                    .eq("epoch_no", epochNo)
                    .query()
                    .stream()
                    .filter(dAppEpoch -> dAppEpoch.getSpendTrxFees() != null)
                    .mapToLong(dappEpoch -> dappEpoch.getSpendTrxFees())
                    .sum();
        } catch (SQLException e) {
            log.error("db error", e);
            throw new RuntimeException(e);
        }
    }

    public long spendTrxSizes(int epochNo) {
        try {
            val statementBuilder = dbManager.getdAppEpochDao()
                    .queryBuilder();

            return statementBuilder
                    .where()
                    .eq("epoch_no", epochNo)
                    .query()
                    .stream()
                    .filter(dAppEpoch -> dAppEpoch.getSpendTrxSizes() != null)
                    .mapToLong(dappEpoch -> dappEpoch.getSpendTrxSizes()).sum();
        } catch (SQLException e) {
            log.error("db error", e);
            throw new RuntimeException(e);
        }
    }
    public void upsertDApp(DAppEpoch dapp) {
        try {
            dbManager.getdAppEpochDao().createOrUpdate(dapp);
        } catch (SQLException e) {
            log.error("db error", e);
            throw new RuntimeException(e);
        }
    }

    public void removeAllExcept(Collection<DAppEpoch> items) {
        dbManager.removeAllExcept(items, () -> dbManager.getdAppEpochDao());
    }

    public void closeEpochs(int currentEpochNo) {
        try {
            dbManager.getdAppEpochDao().executeRaw("UPDATE dapp_epoch SET closed_epoch = true WHERE epoch_no < " + currentEpochNo);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

}
