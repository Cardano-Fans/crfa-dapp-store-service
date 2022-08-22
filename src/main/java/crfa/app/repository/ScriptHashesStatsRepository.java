package crfa.app.repository;

import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.dao.DaoManager;
import com.j256.ormlite.jdbc.JdbcConnectionSource;
import com.j256.ormlite.stmt.QueryBuilder;
import com.j256.ormlite.table.TableUtils;
import crfa.app.domain.ScriptStats;
import io.micronaut.context.annotation.Value;
import io.micronaut.runtime.event.annotation.EventListener;
import io.micronaut.runtime.server.event.ServerStartupEvent;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import lombok.extern.slf4j.Slf4j;

import java.sql.SQLException;
import java.util.Collection;
import java.util.List;

@Singleton
@Slf4j
public class ScriptHashesStatsRepository {

    private JdbcConnectionSource connectionSource;

    @Value("${dbPath-dapps-script-stats:crfa-cardano-dapp-store-script-hashes-stats.db}")
    private String dbPath;

    private Dao<ScriptStats, String> dao;

    @Inject
    private RepositoryColumnConverter repositoryColumnConverter;

    @EventListener
    public void onStartup(ServerStartupEvent event) throws SQLException {
        log.info("Starting ScriptHashesStatsRepository..., dbPath:{}", dbPath);

        String databaseUrl = String.format("jdbc:sqlite:%s", dbPath);
        // create a connection source to our database
        this.connectionSource = new JdbcConnectionSource(databaseUrl);

        this.dao = DaoManager.createDao(connectionSource, ScriptStats.class);

        createDbsIfNecessary();
    }

    public void clearDB() {
        try {
            TableUtils.clearTable(connectionSource, ScriptStats.class);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public List<ScriptStats> listScriptStatsOrderedByTransactionCount() {
        try {
            QueryBuilder<ScriptStats, String> statementBuilder = dao.queryBuilder();

            return statementBuilder
                    .orderBy("transaction_count", false)
                    .query();
        } catch (SQLException e) {
            log.error("db error", e);
            throw new RuntimeException(e);
        }
    }
    public void upsert(ScriptStats scriptStats) {
        try {
            dao.createOrUpdate(scriptStats);
        } catch (SQLException e) {
            log.error("db error", e);
            throw new RuntimeException(e);
        }
    }

    public void upsert(Collection<ScriptStats> scriptStats) {
        try {
            dao.create(scriptStats);
        } catch (SQLException e) {
            log.error("db error", e);
            throw new RuntimeException(e);
        }
    }

    public void createDbsIfNecessary() throws SQLException {
        TableUtils.createTableIfNotExists(this.connectionSource, ScriptStats.class);
    }

}
