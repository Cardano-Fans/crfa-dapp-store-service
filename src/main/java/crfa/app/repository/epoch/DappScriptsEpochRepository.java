package crfa.app.repository.epoch;

import com.j256.ormlite.stmt.QueryBuilder;
import crfa.app.domain.DappScriptItemEpoch;
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

@Singleton
@Slf4j
public class DappScriptsEpochRepository {

    @Inject
    private DbManager dbManager;

    @Inject
    private RepositoryColumnConverter repositoryColumnConverter;

    public List<DappScriptItemEpoch> listDappScriptItems(String releaseKey) {
        return listDappScriptItems(releaseKey, SortBy.SCRIPTS_INVOKED, SortOrder.DESC);
    }

    public List<DappScriptItemEpoch> listByHash(String hash) {
        try {
            QueryBuilder<DappScriptItemEpoch, String> statementBuilder = dbManager.getDappScriptItemEpochs().queryBuilder();

            return statementBuilder
                    .where()
                    .eq("hash", hash)
                    .query();
        } catch (SQLException e) {
            log.error("db error", e);
            throw new RuntimeException(e);
        }
    }

    public List<DappScriptItemEpoch> listDappScriptItems(String releaseKey, SortBy sortBy, SortOrder sortOrder) {
        try {
            val decomposedSortBy = repositoryColumnConverter.decomposeSortBy(sortBy);
            val decomposedSortOrder = repositoryColumnConverter.decomposeSortOrder(sortOrder);

            val statementBuilder = dbManager.getDappScriptItemEpochs().queryBuilder();

            return statementBuilder
                    .orderBy(decomposedSortBy, decomposedSortOrder)
                    .where()
                    .eq("release_key", releaseKey)
                    .query();
        } catch (SQLException e) {
            log.error("db error", e);
            throw new RuntimeException(e);
        }
    }

    public long dappScriptsEpochsCount(String releaseKey) {
        try {
            QueryBuilder<DappScriptItemEpoch, String> statementBuilder = dbManager.getDappScriptItemEpochs().queryBuilder();

            return statementBuilder
                    .where()
                    .eq("release_key", releaseKey)
                    .countOf();
        } catch (SQLException e) {
            log.error("db error", e);
            throw new RuntimeException(e);
        }
    }

    public List<DappScriptItemEpoch> listDappScriptItems(SortBy sortBy, SortOrder sortOrder) {
        try {
            val decomposedSortBy = repositoryColumnConverter.decomposeSortBy(sortBy);
            val decomposedSortOrder = repositoryColumnConverter.decomposeSortOrder(sortOrder);

            val statementBuilder = dbManager.getDappScriptItemEpochs().queryBuilder();

            return statementBuilder
                    .orderBy(decomposedSortBy, decomposedSortOrder)
                    .query();
        } catch (SQLException e) {
            log.error("db error", e);
            throw new RuntimeException(e);
        }
    }

    public List<DappScriptItemEpoch> listDappScriptItems() {
        return listDappScriptItems(SortBy.SCRIPTS_INVOKED, SortOrder.DESC);
    }

    public void update(DappScriptItemEpoch dappScriptItem) {
        try {
            dbManager.getDappScriptItemEpochs().createOrUpdate(dappScriptItem);
        } catch (SQLException e) {
            log.error("db error", e);
            throw new RuntimeException(e);
        }
    }

    public void removeAllExcept(Collection<DappScriptItemEpoch> dappScriptItems) {
        dbManager.removeAllExcept(dappScriptItems, () -> dbManager.getDappScriptItemEpochs());
    }

    public void closeEpochs(int currentEpochNo) {
        try {
            dbManager.getDappScriptItemEpochs().executeRaw("UPDATE dapp_script_item_epoch SET closed_epoch = true WHERE epoch_no < " + currentEpochNo);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

}
