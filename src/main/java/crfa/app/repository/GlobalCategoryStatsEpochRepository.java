package crfa.app.repository;

import crfa.app.domain.GlobalCategoryEpochStats;
import crfa.app.domain.GlobalCategoryStats;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import lombok.extern.slf4j.Slf4j;
import lombok.val;

import java.sql.SQLException;
import java.util.List;

@Singleton
@Slf4j
public class GlobalCategoryStatsEpochRepository {

    @Inject
    private DbManager dbManager;

    public List<GlobalCategoryEpochStats> list() {
        try {
            val statementBuilder = dbManager.getGlobalCategoryStatsEpochDao().queryBuilder();

            return statementBuilder
                    .query()
                    .stream()
                    .toList();
        } catch (SQLException e) {
            log.error("db error", e);
            throw new RuntimeException(e);
        }
    }

    public void upsert(GlobalCategoryEpochStats stats) {
        try {
            dbManager.getGlobalCategoryStatsEpochDao().createOrUpdate(stats);
        } catch (SQLException e) {
            log.error("db error", e);
            throw new RuntimeException(e);
        }
    }

}
