package crfa.app.repository;

import crfa.app.domain.GlobalStatsEpoch;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import lombok.extern.slf4j.Slf4j;
import lombok.val;

import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

@Singleton
@Slf4j
public class GlobalStatsEpochRepository {

    @Inject
    private DbManager dbManager;

    public Optional<GlobalStatsEpoch> findGlobalStatsByEpochNo(int epochNo) {
        try {
            val statementBuilder = dbManager.getGlobalStatsEpochDao()
                    .queryBuilder();

            return statementBuilder
                    .where()
                    .eq("epoch_no", epochNo)
                    .query()
                    .stream()
                    .findFirst();
        } catch (SQLException e) {
            log.error("db error", e);
            throw new RuntimeException(e);
        }
    }

    public List<GlobalStatsEpoch> listGlobalStats() {
        try {
            val statementBuilder = dbManager.getGlobalStatsEpochDao()
                    .queryBuilder();

            return statementBuilder
                    .query();
        } catch (SQLException e) {
            log.error("db error", e);
            throw new RuntimeException(e);
        }
    }

    public void upsert(GlobalStatsEpoch scriptStats) {
        try {
            dbManager.getGlobalStatsEpochDao().createOrUpdate(scriptStats);
        } catch (SQLException e) {
            log.error("db error", e);
            throw new RuntimeException(e);
        }
    }

}
