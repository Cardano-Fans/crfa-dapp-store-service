package crfa.app.repository.total;

import com.j256.ormlite.stmt.QueryBuilder;
import crfa.app.domain.ScriptStats;
import crfa.app.domain.ScriptStatsType;
import crfa.app.repository.DbManager;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import lombok.extern.slf4j.Slf4j;

import java.sql.SQLException;
import java.util.Collection;
import java.util.List;

@Singleton
@Slf4j
public class ScriptHashesStatsRepository {

    @Inject
    private DbManager dbManager;

    public void clearDB() {
        dbManager.clearDB(ScriptStats.class);
    }

    public List<ScriptStats> listScriptStatsOrderedByTransactionCount(ScriptStatsType type) {
        try {
            QueryBuilder<ScriptStats, String> statementBuilder = dbManager.getScriptsStatsDao().queryBuilder();

            return statementBuilder
                    .orderBy("count", false)
                    .where().eq("type", type)
                    .query();
        } catch (SQLException e) {
            log.error("db error", e);
            throw new RuntimeException(e);
        }
    }
    public void upsert(ScriptStats scriptStats) {
        try {
            dbManager.getScriptsStatsDao().createOrUpdate(scriptStats);
        } catch (SQLException e) {
            log.error("db error", e);
            throw new RuntimeException(e);
        }
    }

    public void removeAllExcept(Collection<ScriptStats> items) {
        dbManager.removeAllExcept(items, () -> dbManager.getScriptsStatsDao());
    }

}
