package crfa.app.repository;

import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.stmt.QueryBuilder;
import crfa.app.domain.DApp;
import crfa.app.domain.DappAggrType;
import crfa.app.domain.SortBy;
import crfa.app.domain.SortOrder;
import crfa.app.resource.InvalidParameterException;
import io.micronaut.runtime.event.annotation.EventListener;
import io.micronaut.runtime.server.event.ServerStartupEvent;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import lombok.extern.slf4j.Slf4j;
import lombok.val;

import java.sql.SQLException;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

@Singleton
@Slf4j
public class DappsRepository {

    @Inject
    private DbManager dbManager;

    @Inject
    private RepositoryColumnConverter repositoryColumnConverter;

    public Long totalScriptsLocked() {
        QueryBuilder<DApp, String> statementBuilder = dbManager.getdAppDao().queryBuilder();

        try {
            return statementBuilder.query()
                    .stream().map(DApp::getScriptsLocked).reduce(0L, Long::sum);
        } catch (SQLException e) {
            log.error("db error", e);
            throw new RuntimeException(e);
        }
    }

    public Long totalContractTransactionsCount() {
        QueryBuilder<DApp, String> statementBuilder = dbManager.getdAppDao().queryBuilder();

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
        QueryBuilder<DApp, String> statementBuilder = dbManager.getdAppDao().queryBuilder();

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
            QueryBuilder<DApp, String> statementBuilder = dbManager.getdAppDao().queryBuilder();

            statementBuilder
                    .where().eq("id", id);

            return dbManager.getdAppDao().query(statementBuilder.prepare()).stream().findFirst();
        } catch (SQLException e) {
            log.error("db error", e);
            throw new RuntimeException(e);
        }
    }

    public List<DApp> listDapps(Optional<SortBy> sortBy, Optional<SortOrder> sortOrder, DappAggrType dappAggrType) throws InvalidParameterException {
        val decomposedSortBy = repositoryColumnConverter.decomposeSortBy(sortBy, dappAggrType);
        val decomposedSortOrder = repositoryColumnConverter.decomposeSortOrder(sortOrder);

        if (decomposedSortBy.isEmpty()) {
            throw new InvalidParameterException("Invalid sortBy, valid values: " + Arrays.asList(SortBy.values()));
        }
        if (decomposedSortOrder.isEmpty()) {
            throw new InvalidParameterException("Invalid sortOrder, valid values: " + Arrays.asList(SortOrder.values()));
        }

        try {
            val statementBuilder = dbManager.getdAppDao().queryBuilder();

            val columnName = decomposedSortBy.get();
            val ascending = decomposedSortOrder.get();

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
            dbManager.getdAppDao().createOrUpdate(dapp);
        } catch (SQLException e) {
            log.error("db error", e);
            throw new RuntimeException(e);
        }
    }

}
