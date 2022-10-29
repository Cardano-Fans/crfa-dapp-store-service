package crfa.app.repository.total;

import crfa.app.domain.ScriptStats;
import crfa.app.domain.ScriptStatsType;
import crfa.app.domain.ScriptType;
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
public class ScriptHashesStatsRepository {

    @Inject
    private DbManager dbManager;

    public void clearDB() {
        dbManager.clearDB(ScriptStats.class);
    }

    public List<ScriptStats> listScriptStatsOrderedByTransactionCount(ScriptStatsType type, ScriptType scriptType) {
        try {
            val statementBuilder = dbManager.getScriptsStatsDao().queryBuilder();

            return statementBuilder
                    .orderBy("count", false)
                    .where()
                    .eq("type", type)
                    .and()
                    .eq("script_type", scriptType)
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
