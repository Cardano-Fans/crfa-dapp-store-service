package crfa.app.repository;

import com.j256.ormlite.stmt.QueryBuilder;
import crfa.app.domain.ScriptStats;
import crfa.app.domain.ScriptStatsType;
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
            QueryBuilder<ScriptStats, String> statementBuilder = dbManager.getScriptsDao().queryBuilder();

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
            dbManager.getScriptsDao().createOrUpdate(scriptStats);
        } catch (SQLException e) {
            log.error("db error", e);
            throw new RuntimeException(e);
        }
    }

    public void upsert(Collection<ScriptStats> scriptStats) {
        try {
            dbManager.getScriptsDao().create(scriptStats);
        } catch (SQLException e) {
            log.error("db error", e);
            throw new RuntimeException(e);
        }
    }

}
