package crfa.app.repository;

import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.dao.DaoManager;
import com.j256.ormlite.jdbc.JdbcConnectionSource;
import com.j256.ormlite.stmt.QueryBuilder;
import com.j256.ormlite.table.TableUtils;
import crfa.app.domain.DAppReleaseItem;
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
public class DappReleaseItemRepository {

    private JdbcConnectionSource connectionSource;

    @Value("${dbPath-dapps-release-items:crfa-cardano-dapp-store-dapps-release-items.db}")
    private String dbPath;

    private Dao<DAppReleaseItem, String> dappReleaseItemDao;

    @Inject
    private RepositoryColumnConverter repositoryColumnConverter;

    @EventListener
    public void onStartup(ServerStartupEvent event) throws SQLException {
        log.info("Starting DappReleaseItemRepository..., dbPath:{}", dbPath);

        String databaseUrl = String.format("jdbc:sqlite:%s", dbPath);
        // create a connection source to our database
        this.connectionSource = new JdbcConnectionSource(databaseUrl);

        this.dappReleaseItemDao = DaoManager.createDao(connectionSource, DAppReleaseItem.class);

        createDbsIfNecessary();
    }

    public List<DAppReleaseItem> listReleaseItemsByReleaseKey(String releaseKey, Optional<SortBy> sortBy, Optional<SortOrder> sortOrder) throws InvalidParameterException {
        try {
            var decomposedSortBy = repositoryColumnConverter.decomposeSortBy(sortBy);
            var decomposedSortOrder = repositoryColumnConverter.decomposeSortOrder(sortOrder);

            if (decomposedSortBy.isEmpty()) {
                throw new InvalidParameterException("Invalid sortBy, valid values: " + Arrays.asList(SortBy.values()));
            }
            if (decomposedSortOrder.isEmpty()) {
                throw new InvalidParameterException("Invalid sortOrder, valid values: " + Arrays.asList(SortOrder.values()));
            }

            QueryBuilder<DAppReleaseItem, String> statementBuilder = dappReleaseItemDao.queryBuilder();

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
            var decomposedSortBy = repositoryColumnConverter.decomposeSortBy(sortBy);
            var decomposedSortOrder = repositoryColumnConverter.decomposeSortOrder(sortOrder);

            if (decomposedSortBy.isEmpty()) {
                throw new InvalidParameterException("Invalid sortBy, valid values: " + Arrays.asList(SortBy.values()));
            }
            if (decomposedSortOrder.isEmpty()) {
                throw new InvalidParameterException("Invalid sortOrder, valid values: " + Arrays.asList(SortOrder.values()));
            }

            QueryBuilder<DAppReleaseItem, String> statementBuilder = dappReleaseItemDao.queryBuilder();

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
            dappReleaseItemDao.createOrUpdate(dAppReleaseItem);
        } catch (SQLException e) {
            log.error("db error", e);
            throw new RuntimeException(e);
        }
    }

    public void createDbsIfNecessary() throws SQLException {
        TableUtils.createTableIfNotExists(this.connectionSource, DAppReleaseItem.class);
    }

}
