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

    public Long balance() {
        val statementBuilder = dbManager.getdAppDao().queryBuilder();

        try {
            return statementBuilder.query()
                    .stream()
                    .filter(dApp -> dApp.getBalance() != null)
                    .mapToLong(DApp::getBalance)
                    .sum();
        } catch (SQLException e) {
            log.error("db error", e);
            throw new RuntimeException(e);
        }
    }

    public Long spendTransactions() {
        val statementBuilder = dbManager.getdAppDao().queryBuilder();

        try {
            return statementBuilder.query()
                    .stream()
                    .filter(dApp -> dApp.getSpendTransactions() != null)
                    .mapToLong(DApp::getSpendTransactions)
                    .sum();
        } catch (SQLException e) {
            log.error("db error", e);
            throw new RuntimeException(e);
        }
    }

    public Long mintTransactions() {
        val statementBuilder = dbManager.getdAppDao().queryBuilder();

        try {
            return statementBuilder.query()
                    .stream()
                    .filter(dApp -> dApp.getMintTransactions() != null)
                    .mapToLong(DApp::getMintTransactions)
                    .sum();
        } catch (SQLException e) {
            log.error("db error", e);
            throw new RuntimeException(e);
        }
    }

    public Long spendTrxSizes() {
        val statementBuilder = dbManager.getdAppDao().queryBuilder();

        try {
            return statementBuilder.query()
                    .stream()
                    .filter(dApp -> dApp.getSpendTrxSizes() != null)
                    .mapToLong(DApp::getSpendTrxSizes)
                    .sum();
        } catch (SQLException e) {
            log.error("db error", e);
            throw new RuntimeException(e);
        }
    }

    public Long spendVolume() {
        val statementBuilder = dbManager.getdAppDao().queryBuilder();

        try {
            return statementBuilder.query()
                    .stream()
                    .filter(dApp -> dApp.getSpendVolume() != null)
                    .mapToLong(DApp::getSpendVolume)
                    .sum();
        } catch (SQLException e) {
            log.error("db error", e);
            throw new RuntimeException(e);
        }
    }

    public Long spendTrxFees() {
        val statementBuilder = dbManager.getdAppDao().queryBuilder();

        try {
            return statementBuilder.query()
                    .stream()
                    .filter(dApp -> dApp.getSpendTrxFees() != null)
                    .mapToLong(DApp::getSpendTrxFees)
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

            decomposedSortBy.forEach(column -> statementBuilder.orderBy(column, decomposedSortOrder));

            return statementBuilder.query();
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
