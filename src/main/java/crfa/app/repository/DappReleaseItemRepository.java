package crfa.app.repository;

import com.j256.ormlite.stmt.QueryBuilder;
import crfa.app.domain.DAppReleaseItem;
import crfa.app.domain.SortBy;
import crfa.app.domain.SortOrder;
import crfa.app.resource.InvalidParameterException;
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
public class DappReleaseItemRepository {

    @Inject
    private DbManager dbManager;

    @Inject
    private RepositoryColumnConverter repositoryColumnConverter;

    public List<DAppReleaseItem> listReleaseItemsByReleaseKey(String releaseKey, Optional<SortBy> sortBy, Optional<SortOrder> sortOrder) throws InvalidParameterException {
        try {
            val decomposedSortBy = repositoryColumnConverter.decomposeSortBy(sortBy);
            val decomposedSortOrder = repositoryColumnConverter.decomposeSortOrder(sortOrder);

            if (decomposedSortBy.isEmpty()) {
                throw new InvalidParameterException("Invalid sortBy, valid values: " + Arrays.asList(SortBy.values()));
            }
            if (decomposedSortOrder.isEmpty()) {
                throw new InvalidParameterException("Invalid sortOrder, valid values: " + Arrays.asList(SortOrder.values()));
            }

            QueryBuilder<DAppReleaseItem, String> statementBuilder = dbManager.getDappReleaseItemDao().queryBuilder();

            return statementBuilder
                    .orderBy(decomposedSortBy.get(), decomposedSortOrder.get())
                    .where().eq("release_key", releaseKey)
                    .query();
        } catch (SQLException e) {
            log.error("db error", e);
            throw new RuntimeException(e);
        }
    }

    public List<DAppReleaseItem> listReleaseItems(Optional<SortBy> sortBy, Optional<SortOrder> sortOrder) throws InvalidParameterException {
        try {
            val decomposedSortBy = repositoryColumnConverter.decomposeSortBy(sortBy);
            val decomposedSortOrder = repositoryColumnConverter.decomposeSortOrder(sortOrder);

            if (decomposedSortBy.isEmpty()) {
                throw new InvalidParameterException("Invalid sortBy, valid values: " + Arrays.asList(SortBy.values()));
            }
            if (decomposedSortOrder.isEmpty()) {
                throw new InvalidParameterException("Invalid sortOrder, valid values: " + Arrays.asList(SortOrder.values()));
            }

            QueryBuilder<DAppReleaseItem, String> statementBuilder = dbManager.getDappReleaseItemDao().queryBuilder();

            return statementBuilder
                    .orderBy(decomposedSortBy.get(), decomposedSortOrder.get())
                    .query();
        } catch (SQLException e) {
            log.error("db error", e);
            throw new RuntimeException(e);
        }
    }

    public List<DAppReleaseItem> listReleaseItems() {
        try {
            return listReleaseItems(Optional.empty(), Optional.empty());
        } catch (InvalidParameterException e) {
            throw new RuntimeException(e);
        }
    }

    public void updatedAppReleaseItem(DAppReleaseItem dAppReleaseItem) {
        try {
            dbManager.getDappReleaseItemDao().createOrUpdate(dAppReleaseItem);
        } catch (SQLException e) {
            log.error("db error", e);
            throw new RuntimeException(e);
        }
    }

}
