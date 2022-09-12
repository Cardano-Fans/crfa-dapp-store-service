package crfa.app.repository;

import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.dao.DaoManager;
import com.j256.ormlite.jdbc.JdbcConnectionSource;
import com.j256.ormlite.stmt.QueryBuilder;
import com.j256.ormlite.table.TableUtils;
import crfa.app.domain.DAppRelease;
import crfa.app.domain.SortBy;
import crfa.app.domain.SortOrder;
import crfa.app.resource.InvalidParameterException;
import io.micronaut.context.annotation.Value;
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
public class DappReleasesRepository {

    private JdbcConnectionSource connectionSource;

    @Value("${dbPath-dapps-releases:crfa-cardano-dapp-store-dapps-releases.db}")
    private String dbPath;

    private Dao<DAppRelease, String> dAppResultItemDao;

    @Inject
    private RepositoryColumnConverter repositoryColumnConverter;

    @EventListener
    public void onStartup(ServerStartupEvent event) throws SQLException {
        log.info("Starting DappResultItemRepository..., dbPath:{}", dbPath);

        String databaseUrl = String.format("jdbc:sqlite:%s", dbPath);
        // create a connection source to our database
        this.connectionSource = new JdbcConnectionSource(databaseUrl);

        this.dAppResultItemDao = DaoManager.createDao(connectionSource, DAppRelease.class);

        createDbsIfNecessary();
    }

    public float getMaxReleaseVersion(String id) {
        QueryBuilder<DAppRelease, String> statementBuilder = dAppResultItemDao.queryBuilder();

        try {
            return Optional.ofNullable(statementBuilder
                    .selectColumns("release_number")
                    .orderBy("release_number", false)
                    .limit(1L)
                    .where().eq("id", id)
                    .queryForFirst())
                    .map(DAppRelease::getReleaseNumber)
                    .orElse(-1.0f);
        } catch (SQLException e) {
            log.error("db error", e);
            throw new RuntimeException(e);
        }
    }

    public Optional<DAppRelease> findByReleaseKey(String releaseKey) {
        try {
            QueryBuilder<DAppRelease, String> statementBuilder = dAppResultItemDao.queryBuilder();

            statementBuilder
                    .where().eq("key", releaseKey);

            return dAppResultItemDao.query(statementBuilder.prepare()).stream().findFirst();
        } catch (SQLException e) {
            log.error("db error", e);
            throw new RuntimeException(e);
        }
    }

    public List<DAppRelease> listDappReleases(Optional<SortBy> sortBy, Optional<SortOrder> sortOrder) throws InvalidParameterException {
        val decomposedSortBy = repositoryColumnConverter.decomposeSortBy(sortBy);
        val decomposedSortOrder = repositoryColumnConverter.decomposeSortOrder(sortOrder);

        if (decomposedSortBy.isEmpty()) {
            throw new InvalidParameterException("Invalid sortBy, valid values: " + Arrays.asList(SortBy.values()));
        }
        if (decomposedSortOrder.isEmpty()) {
            throw new InvalidParameterException("Invalid sortOrder, valid values: " + Arrays.asList(SortOrder.values()));
        }

        try {
            QueryBuilder<DAppRelease, String> statementBuilder = dAppResultItemDao.queryBuilder();

            return statementBuilder
                    .orderBy(decomposedSortBy.get(), decomposedSortOrder.get())
                    .query();
        } catch (SQLException e) {
            log.error("db error", e);
            throw new RuntimeException(e);
        }
    }

    public void upsertDAppRelease(DAppRelease dAppRelease) {
        try {
            dAppResultItemDao.createOrUpdate(dAppRelease);
        } catch (SQLException e) {
            log.error("db error", e);
            throw new RuntimeException(e);
        }
    }

    public void createDbsIfNecessary() throws SQLException {
        TableUtils.createTableIfNotExists(this.connectionSource, DAppRelease.class);
    }

}
