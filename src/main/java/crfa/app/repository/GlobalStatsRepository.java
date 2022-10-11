package crfa.app.repository;

import crfa.app.domain.GlobalStats;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import lombok.extern.slf4j.Slf4j;
import lombok.val;

import java.sql.SQLException;
import java.util.Optional;

@Singleton
@Slf4j
public class GlobalStatsRepository {

    @Inject
    private DbManager dbManager;

    public Optional<GlobalStats> returnGlobalStats() {
        try {
            val statementBuilder = dbManager.getGlobalStatsDao().queryBuilder();

            return statementBuilder
                    .query()
                    .stream()
                    .findFirst();
        } catch (SQLException e) {
            log.error("db error", e);
            throw new RuntimeException(e);
        }
    }

    public void upsert(GlobalStats scriptStats) {
        try {
            dbManager.getGlobalStatsDao().createOrUpdate(scriptStats);
        } catch (SQLException e) {
            log.error("db error", e);
            throw new RuntimeException(e);
        }
    }

}
