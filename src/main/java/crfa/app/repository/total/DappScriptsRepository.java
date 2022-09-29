package crfa.app.repository.total;

import com.j256.ormlite.stmt.QueryBuilder;
import crfa.app.domain.DappScriptItem;
import crfa.app.domain.SortBy;
import crfa.app.domain.SortOrder;
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
public class DappScriptsRepository {

    @Inject
    private DbManager dbManager;

    @Inject
    private RepositoryColumnConverter repositoryColumnConverter;

    public List<DappScriptItem> listDappScriptItems(String releaseKey, Optional<SortBy> sortBy, Optional<SortOrder> sortOrder) throws InvalidParameterException {
        try {
            val decomposedSortBy = repositoryColumnConverter.decomposeSortBy(sortBy);
            val decomposedSortOrder = repositoryColumnConverter.decomposeSortOrder(sortOrder);

            if (decomposedSortBy.isEmpty()) {
                throw new InvalidParameterException("Invalid sortBy, valid values: " + Arrays.asList(SortBy.values()));
            }
            if (decomposedSortOrder.isEmpty()) {
                throw new InvalidParameterException("Invalid sortOrder, valid values: " + Arrays.asList(SortOrder.values()));
            }

            QueryBuilder<DappScriptItem, String> statementBuilder = dbManager.getDappScriptItems().queryBuilder();

            return statementBuilder
                    .orderBy(decomposedSortBy.get(), decomposedSortOrder.get())
                    .where().eq("release_key", releaseKey)
                    .query();
        } catch (SQLException e) {
            log.error("db error", e);
            throw new RuntimeException(e);
        }
    }

    public List<DappScriptItem> listDappScriptItems(Optional<SortBy> sortBy, Optional<SortOrder> sortOrder) throws InvalidParameterException {
        try {
            val decomposedSortBy = repositoryColumnConverter.decomposeSortBy(sortBy);
            val decomposedSortOrder = repositoryColumnConverter.decomposeSortOrder(sortOrder);

            if (decomposedSortBy.isEmpty()) {
                throw new InvalidParameterException("Invalid sortBy, valid values: " + Arrays.asList(SortBy.values()));
            }
            if (decomposedSortOrder.isEmpty()) {
                throw new InvalidParameterException("Invalid sortOrder, valid values: " + Arrays.asList(SortOrder.values()));
            }

            QueryBuilder<DappScriptItem, String> statementBuilder = dbManager.getDappScriptItems().queryBuilder();

            return statementBuilder
                    .orderBy(decomposedSortBy.get(), decomposedSortOrder.get())
                    .query();
        } catch (SQLException e) {
            log.error("db error", e);
            throw new RuntimeException(e);
        }
    }

    public List<DappScriptItem> listDappScriptItems() {
        try {
            return listDappScriptItems(Optional.empty(), Optional.empty());
        } catch (InvalidParameterException e) {
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
