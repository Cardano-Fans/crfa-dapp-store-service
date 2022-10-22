package crfa.app.repository.total;

import crfa.app.domain.DApp;
import crfa.app.domain.SortBy;
import crfa.app.domain.SortOrder;
import crfa.app.repository.DbManager;
import crfa.app.repository.RepositoryColumnConverter;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import lombok.extern.slf4j.Slf4j;
import lombok.val;

import java.sql.SQLException;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static java.util.stream.Collectors.toSet;

@Singleton
@Slf4j
public class DappsRepository {

    @Inject
    private DbManager dbManager;

    @Inject
    private RepositoryColumnConverter repositoryColumnConverter;

    public Set<String> allCategories() {
        return listDapps().stream().map(DApp::getCategory).collect(toSet());
    }

    public Long totalScriptsLocked() {
        val statementBuilder = dbManager.getdAppDao().queryBuilder();

        try {
            return statementBuilder.query()
                    .stream()
                    .filter(dApp -> dApp.getScriptsLocked() != null)
                    .mapToLong(DApp::getScriptsLocked)
                    .sum();
        } catch (SQLException e) {
            log.error("db error", e);
            throw new RuntimeException(e);
        }
    }

    public Long totalScriptInvocations() {
        val statementBuilder = dbManager.getdAppDao().queryBuilder();

        try {
            return statementBuilder.query()
                    .stream()
                    .mapToLong(DApp::getScriptInvocationsCount)
                    .sum();
        } catch (SQLException e) {
            log.error("db error", e);
            throw new RuntimeException(e);
        }
    }

    public Long totalTrxSizes() {
        val statementBuilder = dbManager.getdAppDao().queryBuilder();

        try {
            return statementBuilder.query()
                    .stream()
                    .filter(dApp -> dApp.getTrxSizes() != null)
                    .mapToLong(DApp::getTrxSizes)
                    .sum();
        } catch (SQLException e) {
            log.error("db error", e);
            throw new RuntimeException(e);
        }
    }

    public Long volume() {
        val statementBuilder = dbManager.getdAppDao().queryBuilder();

        try {
            return statementBuilder.query()
                    .stream()
                    .filter(dApp -> dApp.getVolume() != null)
                    .mapToLong(DApp::getVolume)
                    .sum();
        } catch (SQLException e) {
            log.error("db error", e);
            throw new RuntimeException(e);
        }
    }

    public Long fees() {
        val statementBuilder = dbManager.getdAppDao().queryBuilder();

        try {
            return statementBuilder.query()
                    .stream()
                    .filter(dApp -> dApp.getFees() != null)
                    .mapToLong(DApp::getFees)
                    .sum();
        } catch (SQLException e) {
            log.error("db error", e);
            throw new RuntimeException(e);
        }
    }

    public Optional<DApp> findById(String id) {
        try {
            val statementBuilder = dbManager.getdAppDao().queryBuilder();

            statementBuilder
                    .where().eq("id", id);

            return dbManager.getdAppDao().query(statementBuilder.prepare()).stream().findFirst();
        } catch (SQLException e) {
            log.error("db error", e);
            throw new RuntimeException(e);
        }
    }

    public List<DApp> listDapps() {
        return listDapps(SortBy.SCRIPTS_INVOKED, SortOrder.DESC);
    }

    public List<DApp> listDapps(SortBy sortBy, SortOrder sortOrder) {
        val decomposedSortBy = repositoryColumnConverter.decomposeSortBy(sortBy);
        val decomposedSortOrder = repositoryColumnConverter.decomposeSortOrder(sortOrder);

        try {
            val statementBuilder = dbManager.getdAppDao().queryBuilder();

            return statementBuilder
                    .orderBy(decomposedSortBy, decomposedSortOrder)
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

    public void removeAllExcept(Collection<DApp> items) {
        dbManager.removeAllExcept(items, () -> dbManager.getdAppDao());
    }

}
