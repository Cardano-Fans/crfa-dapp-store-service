package crfa.app.repository.epoch;

import com.j256.ormlite.stmt.QueryBuilder;
import crfa.app.domain.DApp;
import crfa.app.domain.DAppEpoch;
import crfa.app.repository.DbManager;
import crfa.app.repository.RepositoryColumnConverter;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import lombok.extern.slf4j.Slf4j;
import lombok.val;

import java.sql.SQLException;
import java.util.Collection;
import java.util.List;

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

//    public List<DAppEpoch> findByDappId(String dappId, int fromEpoch) {
//        try {
//            QueryBuilder<DAppEpoch, String> statementBuilder = dbManager.getdAppEpochDao().queryBuilder();
//
//            statementBuilder
//                    .where()
//                    .eq("dapp_id", dappId)
//                    .and()
//                    .ge("epoch_no", fromEpoch);
//
//            return dbManager.getdAppEpochDao().query(statementBuilder.prepare());
//        } catch (SQLException e) {
//            log.error("db error", e);
//            throw new RuntimeException(e);
//        }
//    }

//    public long dappEpochsCount(String dappId) {
//        try {
//            QueryBuilder<DAppEpoch, String> statementBuilder = dbManager.getdAppEpochDao().queryBuilder();
//
//            return statementBuilder
//                    .where()
//                    .eq("dapp_id", dappId)
//                    .countOf();
//        } catch (SQLException e) {
//            log.error("db error", e);
//            throw new RuntimeException(e);
//        }
//    }

//    public List<DAppEpoch> listDapps(Optional<SortBy> sortBy, Optional<SortOrder> sortOrder) throws InvalidParameterException {
//        val decomposedSortBy = repositoryColumnConverter.decomposeSortBy(sortBy);
//        val decomposedSortOrder = repositoryColumnConverter.decomposeSortOrder(sortOrder);
//
//        if (decomposedSortBy.isEmpty()) {
//            throw new InvalidParameterException("Invalid sortBy, valid values: " + Arrays.asList(SortBy.values()));
//        }
//        if (decomposedSortOrder.isEmpty()) {
//            throw new InvalidParameterException("Invalid sortOrder, valid values: " + Arrays.asList(SortOrder.values()));
//        }
//
//        try {
//            val statementBuilder = dbManager.getdAppEpochDao().queryBuilder();
//
//            val columnName = decomposedSortBy.get();
//            val ascending = decomposedSortOrder.get();
//
//            return statementBuilder
//                    .orderBy(columnName, ascending)
//                    .query();
//        } catch (SQLException e) {
//            log.error("db error", e);
//            throw new RuntimeException(e);
//        }
//    }

    public long inflowsOutflows(int epochNo) {
        try {
            val statementBuilder = dbManager.getdAppEpochDao()
                    .queryBuilder();

            return statementBuilder
                    .where()
                    .eq("epoch_no", epochNo)
                    .query()
                    .stream()
                    .filter(dAppEpoch -> dAppEpoch.getInflowsOutflows() != null)
                    .mapToLong(dappEpoch -> dappEpoch.getInflowsOutflows().longValue())
                    .reduce(0, Long::sum);
        } catch (SQLException e) {
            log.error("db error", e);
            throw new RuntimeException(e);
        }
    }

    public long totalScriptInvocations(int epochNo) {
        try {
            val statementBuilder = dbManager.getdAppEpochDao()
                .queryBuilder();

            return statementBuilder
                .where()
                .eq("epoch_no", epochNo)
                .query()
                .stream()
                .filter(dAppEpoch -> dAppEpoch.getScriptInvocationsCount() != null)
                .mapToLong(dappEpoch -> dappEpoch.getScriptInvocationsCount().longValue())
                .reduce(0, Long::sum);
        } catch(SQLException e) {
            log.error("db error", e);
            throw new RuntimeException(e);
        }
    }

    public long volume(int epochNo) {
        try {
            val statementBuilder = dbManager.getdAppEpochDao()
                    .queryBuilder();

            return statementBuilder
                    .where()
                    .eq("epoch_no", epochNo)
                    .query()
                    .stream()
                    .filter(dAppEpoch -> dAppEpoch.getVolume() != null)
                    .mapToLong(dappEpoch -> dappEpoch.getVolume().longValue())
                    .reduce(0, Long::sum);
        } catch(SQLException e) {
            log.error("db error", e);
            throw new RuntimeException(e);
        }
    }

    public long fees(int epochNo) {
        try {
            val statementBuilder = dbManager.getdAppEpochDao()
                    .queryBuilder();

            return statementBuilder
                    .where()
                    .eq("epoch_no", epochNo)
                    .query()
                    .stream()
                    .filter(dAppEpoch -> dAppEpoch.getFees() != null)
                    .mapToLong(dappEpoch -> dappEpoch.getVolume().longValue())
                    .reduce(0, Long::sum);
        } catch(SQLException e) {
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
            dbManager.getdAppEpochDao().executeRaw("UPDATE dapp_epoch SET closed_epoch = true WHERE epoch_no < " + currentEpochNo);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

}
