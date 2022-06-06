package crfa.app.repository;

import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.dao.DaoManager;
import com.j256.ormlite.jdbc.JdbcConnectionSource;
import com.j256.ormlite.stmt.QueryBuilder;
import com.j256.ormlite.table.TableUtils;
import crfa.app.domain.DAppRelease;
import crfa.app.domain.SortBy;
import crfa.app.domain.SortOrder;
import io.micronaut.context.annotation.Value;
import io.micronaut.runtime.event.annotation.EventListener;
import io.micronaut.runtime.server.event.ServerStartupEvent;
import jakarta.inject.Singleton;
import lombok.extern.slf4j.Slf4j;

import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

@Singleton
@Slf4j
public class DappReleasesRepository {

    private JdbcConnectionSource connectionSource;

    @Value("${dbPath:crfa-dapp-releases-db-dev.db}")
    private String dbPath;

    private Dao<DAppRelease, String> dAppResultItemDao;

    @EventListener
    public void onStartup(ServerStartupEvent event) throws SQLException {
        log.info("Starting DappResultItemRepository..., dbPath:{}", dbPath);

        String databaseUrl = String.format("jdbc:sqlite:%s", dbPath);
        // create a connection source to our database
        this.connectionSource = new JdbcConnectionSource(databaseUrl);

        this.dAppResultItemDao = DaoManager.createDao(connectionSource, DAppRelease.class);

        createDbsIfNecessary();
    }

    public Long scriptsLocked() throws SQLException {
        QueryBuilder<DAppRelease, String> statementBuilder = dAppResultItemDao.queryBuilder();

        return statementBuilder.query()
                .stream().map(DAppRelease::getScriptsLocked).reduce(0L, Long::sum);
    }

    public Long totalScriptInvocations() throws SQLException {
        QueryBuilder<DAppRelease, String> statementBuilder = dAppResultItemDao.queryBuilder();

        return statementBuilder.query()
                .stream().map(DAppRelease::getScriptInvocationsCount).reduce(0L, Long::sum);
    }

    public Optional<DAppRelease> findByReleaseKey(String releaseKey) {
        try {
            QueryBuilder<DAppRelease, String> statementBuilder = dAppResultItemDao.queryBuilder();

            statementBuilder
                    .where().eq("key", releaseKey);

            return dAppResultItemDao.query(statementBuilder.prepare()).stream().findFirst();
        } catch (SQLException e) {
            log.error("db error", e);
            throw new RuntimeException(e);
        }
    }

    public List<DAppRelease> listDapps(Optional<SortBy> sortBy, Optional<SortOrder> sortOrder) {
        var decomposedSortBy = decomposeSortBy(sortBy);
        var decomposedSortOrder = decomposeSortOrder(sortOrder);

        try {
            QueryBuilder<DAppRelease, String> statementBuilder = dAppResultItemDao.queryBuilder();

            return statementBuilder
                    .orderBy(decomposedSortBy, decomposedSortOrder)
                    .query();
        } catch (SQLException e) {
            log.error("db error", e);
            throw new RuntimeException(e);
        }
    }

    private String decomposeSortBy(Optional<SortBy> sortBy) {
        var sby = sortBy.orElse(SortBy.SCRIPTS_INVOKED);

        if (sby == SortBy.SCRIPTS_INVOKED) {
            return "script_invocations";
        }
        if (sby == SortBy.SCRIPTS_LOCKED) {
            return "scripts_locked";
        }
        if (sby == SortBy.TRANSACTIONS_COUNT) {
            return "trx_count";
        }
        if (sby == SortBy.TOTAL_VALUE_LOCKED) {
            return "total_value_locked";
        }
        if (sby == SortBy.FULL_NAME) {
            return "full_name";
        }

        return "trx_count";
    }

    private boolean decomposeSortOrder(Optional<SortOrder> sortOrder) {
        var so = sortOrder.orElse(SortOrder.DESC);

        if (so == SortOrder.ASC) {
            return true;
        }

        return false;
    }

    public void upsertDAppRelease(DAppRelease dAppRelease) {
        try {
            dAppResultItemDao.createOrUpdate(dAppRelease);
        } catch (SQLException e) {
            log.error("db error", e);
            throw new RuntimeException(e);
        }
    }

    public void createDbsIfNecessary() throws SQLException {
        TableUtils.createTableIfNotExists(this.connectionSource, DAppRelease.class);
    }

}
