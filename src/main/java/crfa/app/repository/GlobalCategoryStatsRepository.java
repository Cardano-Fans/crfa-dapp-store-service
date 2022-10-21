package crfa.app.repository;

import crfa.app.domain.GlobalCategoryStats;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import lombok.extern.slf4j.Slf4j;
import lombok.val;

import java.sql.SQLException;
import java.util.List;

@Singleton
@Slf4j
public class GlobalCategoryStatsRepository {

    @Inject
    private DbManager dbManager;

    public List<GlobalCategoryStats> listGlobalStats() {
        try {
            val statementBuilder = dbManager.getGlobalCategoryStatsDao().queryBuilder();

            return statementBuilder
                    .query()
                    .stream()
                    .toList();
        } catch (SQLException e) {
            log.error("db error", e);
            throw new RuntimeException(e);
        }
    }

    public void upsert(GlobalCategoryStats scriptStats) {
        try {
            dbManager.getGlobalCategoryStatsDao().createOrUpdate(scriptStats);
        } catch (SQLException e) {
            log.error("db error", e);
            throw new RuntimeException(e);
        }
    }

}
