package crfa.app.repository.total;

import com.j256.ormlite.stmt.QueryBuilder;
import crfa.app.domain.DappScriptItem;
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
public class DappScriptsRepository {

    @Inject
    private DbManager dbManager;

    @Inject
    private RepositoryColumnConverter repositoryColumnConverter;

    public List<DappScriptItem> listDappScriptItems(String releaseKey, SortBy sortBy, SortOrder sortOrder) {
        try {
            val decomposedSortBy = repositoryColumnConverter.decomposeSortBy(sortBy);
            val decomposedSortOrder = repositoryColumnConverter.decomposeSortOrder(sortOrder);

            val statementBuilder = dbManager.getDappScriptItems().queryBuilder();

            return statementBuilder
                    .orderBy(decomposedSortBy, decomposedSortOrder)
                    .where().eq("release_key", releaseKey)
                    .query();
        } catch (SQLException e) {
            log.error("db error", e);
            throw new RuntimeException(e);
        }
    }

    public List<DappScriptItem> listDappScriptItems() {
        try {
            QueryBuilder<DappScriptItem, String> statementBuilder = dbManager.getDappScriptItems().queryBuilder();

            return statementBuilder
                    .query();
        } catch (SQLException e) {
            log.error("db error", e);
            throw new RuntimeException(e);
        }
    }

    public List<DappScriptItem> listDappScriptItems(SortBy sortBy, SortOrder sortOrder) {
        try {
            val decomposedSortBy = repositoryColumnConverter.decomposeSortBy(sortBy);
            val decomposedSortOrder = repositoryColumnConverter.decomposeSortOrder(sortOrder);

            val statementBuilder = dbManager.getDappScriptItems().queryBuilder();

            return statementBuilder
                    .orderBy(decomposedSortBy, decomposedSortOrder)
                    .query();
        } catch (SQLException e) {
            log.error("db error", e);
            throw new RuntimeException(e);
        }
    }

    public void update(DappScriptItem dappScriptItem) {
        try {
            dbManager.getDappScriptItems().createOrUpdate(dappScriptItem);
        } catch (SQLException e) {
            log.error("db error", e);
            throw new RuntimeException(e);
        }
    }

    public void removeAllExcept(Collection<DappScriptItem> dappScriptItems) {
        dbManager.removeAllExcept(dappScriptItems, () -> dbManager.getDappScriptItems());
    }

}
