package crfa.app.repository.epoch;

import com.j256.ormlite.stmt.QueryBuilder;
import crfa.app.domain.*;
import crfa.app.repository.DbManager;
import crfa.app.repository.RepositoryColumnConverter;
import crfa.app.resource.InvalidParameterException;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import lombok.extern.slf4j.Slf4j;
import lombok.val;

import java.sql.SQLException;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

@Singleton
@Slf4j
public class DappsEpochRepository {

    @Inject
    private DbManager dbManager;

    @Inject
    private RepositoryColumnConverter repositoryColumnConverter;

    public Long volume() {
        QueryBuilder<DApp, String> statementBuilder = dbManager.getdAppDao().queryBuilder();

        try {
            return statementBuilder.query()
                    .stream()
                    .filter(dApp -> dApp.getVolume() != null)
                    .map(DApp::getVolume).reduce(0L, Long::sum);
        } catch (SQLException e) {
            log.error("db error", e);
            throw new RuntimeException(e);
        }
    }

    public List<DAppEpoch> findByDappId(String dappId) {
        try {
            QueryBuilder<DAppEpoch, String> statementBuilder = dbManager.getdAppEpochDao().queryBuilder();

            statementBuilder
                    .where().eq("dapp_id", dappId)
            ;

            return dbManager.getdAppEpochDao().query(statementBuilder.prepare());
        } catch (SQLException e) {
            log.error("db error", e);
            throw new RuntimeException(e);
        }
    }
    public List<DAppEpoch> findByDappId(String dappId, int fromEpoch) {
        try {
            QueryBuilder<DAppEpoch, String> statementBuilder = dbManager.getdAppEpochDao().queryBuilder();

            statementBuilder
                    .where()
                    .eq("dapp_id", dappId)
                    .and()
                    .ge("epoch_no", fromEpoch);

            return dbManager.getdAppEpochDao().query(statementBuilder.prepare());
        } catch (SQLException e) {
            log.error("db error", e);
            throw new RuntimeException(e);
        }
    }

    public List<DAppEpoch> listDapps(Optional<SortBy> sortBy, Optional<SortOrder> sortOrder, DappAggrType dappAggrType) throws InvalidParameterException {
        val decomposedSortBy = repositoryColumnConverter.decomposeSortBy(sortBy, dappAggrType);
        val decomposedSortOrder = repositoryColumnConverter.decomposeSortOrder(sortOrder);

        if (decomposedSortBy.isEmpty()) {
            throw new InvalidParameterException("Invalid sortBy, valid values: " + Arrays.asList(SortBy.values()));
        }
        if (decomposedSortOrder.isEmpty()) {
            throw new InvalidParameterException("Invalid sortOrder, valid values: " + Arrays.asList(SortOrder.values()));
        }

        try {
            val statementBuilder = dbManager.getdAppEpochDao().queryBuilder();

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

    public void upsertDApp(DAppEpoch dapp) {
        try {
            dbManager.getdAppEpochDao().createOrUpdate(dapp);
        } catch (SQLException e) {
            log.error("db error", e);
            throw new RuntimeException(e);
        }
    }

    public void removeAllExcept(Collection<DAppEpoch> items) {
        dbManager.removeAllExcept(items, () -> dbManager.getdAppEpochDao());
    }

    public void closeEpochs(int currentEpochNo) {
        try {
            dbManager.getdAppEpochDao().executeRaw("UPDATE dapp_epoch SET closed_epoch = true WHERE epoch_no < :currentEpochNo", String.valueOf(currentEpochNo));
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

}
