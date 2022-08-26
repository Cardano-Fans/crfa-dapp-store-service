package crfa.app.repository;

import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.dao.DaoManager;
import com.j256.ormlite.jdbc.JdbcConnectionSource;
import com.j256.ormlite.stmt.QueryBuilder;
import com.j256.ormlite.table.TableUtils;
import crfa.app.domain.DApp;
import crfa.app.domain.DappAggrType;
import crfa.app.domain.SortBy;
import crfa.app.domain.SortOrder;
import crfa.app.resource.InvalidParameterException;
import io.micronaut.context.annotation.Value;
import io.micronaut.runtime.event.annotation.EventListener;
import io.micronaut.runtime.server.event.ServerStartupEvent;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import lombok.extern.slf4j.Slf4j;

import java.sql.SQLException;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

@Singleton
@Slf4j
public class DappsRepository {

    private JdbcConnectionSource connectionSource;

    @Value("${dbPath-dapps:crfa-cardano-dapp-store-dapps.db}")
    private String dbPath;

    private Dao<DApp, String> dAppDao;

    @Inject
    private RepositoryColumnConverter repositoryColumnConverter;

    @EventListener
    public void onStartup(ServerStartupEvent event) throws SQLException {
        log.info("Starting DappsRepository..., dbPath:{}", dbPath);

        String databaseUrl = String.format("jdbc:sqlite:%s", dbPath);
        // create a connection source to our database
        this.connectionSource = new JdbcConnectionSource(databaseUrl);

        this.dAppDao = DaoManager.createDao(connectionSource, DApp.class);

        createDbsIfNecessary();
    }

    public Long totalScriptsLocked() {
        QueryBuilder<DApp, String> statementBuilder = dAppDao.queryBuilder();

        try {
            return statementBuilder.query()
                    .stream().map(DApp::getScriptsLocked).reduce(0L, Long::sum);
        } catch (SQLException e) {
            log.error("db error", e);
            throw new RuntimeException(e);
        }
    }

    public Long totalContractTransactionsCount() {
        QueryBuilder<DApp, String> statementBuilder = dAppDao.queryBuilder();

        try {
            return statementBuilder
                    .query()
                    .stream().map(DApp::getTransactionsCount).reduce(0L, Long::sum);
        } catch (SQLException e) {
            log.error("db error", e);
            throw new RuntimeException(e);
        }
    }

    public Long totalScriptInvocations() {
        QueryBuilder<DApp, String> statementBuilder = dAppDao.queryBuilder();

        try {
            return statementBuilder.query()
                    .stream().map(DApp::getScriptInvocationsCount).reduce(0L, Long::sum);
        } catch (SQLException e) {
            log.error("db error", e);
            throw new RuntimeException(e);
        }
    }

    public Optional<DApp> findById(String id) {
        try {
            QueryBuilder<DApp, String> statementBuilder = dAppDao.queryBuilder();

            statementBuilder
                    .where().eq("id", id);

            return dAppDao.query(statementBuilder.prepare()).stream().findFirst();
        } catch (SQLException e) {
            log.error("db error", e);
            throw new RuntimeException(e);
        }
    }

    public List<DApp> listDapps(Optional<SortBy> sortBy, Optional<SortOrder> sortOrder, DappAggrType dappAggrType) throws InvalidParameterException {
        var decomposedSortBy = repositoryColumnConverter.decomposeSortBy(sortBy, dappAggrType);
        var decomposedSortOrder = repositoryColumnConverter.decomposeSortOrder(sortOrder);

        if (decomposedSortBy.isEmpty()) {
            throw new InvalidParameterException("Invalid sortBy, valid values: " + Arrays.asList(SortBy.values()));
        }
        if (decomposedSortOrder.isEmpty()) {
            throw new InvalidParameterException("Invalid sortOrder, valid values: " + Arrays.asList(SortOrder.values()));
        }

        try {
            var statementBuilder = dAppDao.queryBuilder();

            final var columnName = decomposedSortBy.get();
            final var ascending = decomposedSortOrder.get();

            return statementBuilder
                    .orderBy(columnName, ascending)
                    .query();
        } catch (SQLException e) {
            log.error("db error", e);
            throw new RuntimeException(e);
        }
    }

    public void upsertDApp(DApp dapp) {
        try {
            dAppDao.createOrUpdate(dapp);
        } catch (SQLException e) {
            log.error("db error", e);
            throw new RuntimeException(e);
        }
    }

    public void createDbsIfNecessary() throws SQLException {
        TableUtils.createTableIfNotExists(this.connectionSource, DApp.class);
    }

}
