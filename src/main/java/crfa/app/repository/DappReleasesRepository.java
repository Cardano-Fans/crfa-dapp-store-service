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

import java.sql.SQLException;
import java.util.*;

import static crfa.app.domain.SortBy.SCRIPTS_INVOKED;
import static crfa.app.domain.SortOrder.ASC;
import static java.util.Comparator.comparingInt;
import static java.util.stream.Collectors.collectingAndThen;
import static java.util.stream.Collectors.toCollection;

@Singleton
@Slf4j
public class DappReleasesRepository {

    private JdbcConnectionSource connectionSource;

    @Value("${dbPath:crfa-dapp-releases-db-dev.db}")
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

    // TODO this is service method, not really repo method
    public List<DAppRelease> dappUniqueReleases() {
        try {
            return listDappReleases(Optional.of(SCRIPTS_INVOKED), Optional.of(ASC))
                    .stream()
                    .collect(collectingAndThen(toCollection(() -> new TreeSet<>(comparingInt(d -> d.getId().hashCode()))),
                            ArrayList::new));
        } catch (InvalidParameterException e) {
            throw new RuntimeException("should never happen", e);
        }
    }

    public Long totalScriptsLocked() {
        QueryBuilder<DAppRelease, String> statementBuilder = dAppResultItemDao.queryBuilder();

        try {
            return statementBuilder.query()
                    .stream().map(DAppRelease::getScriptsLocked).reduce(0L, Long::sum);
        } catch (SQLException e) {
            log.error("db error", e);
            throw new RuntimeException(e);
        }
    }

    public Long totalContractTransactionsCount() {
        QueryBuilder<DAppRelease, String> statementBuilder = dAppResultItemDao.queryBuilder();

        try {
            return statementBuilder
                    .query()
                    .stream().map(DAppRelease::getTransactionsCount).reduce(0L, Long::sum);
        } catch (SQLException e) {
            log.error("db error", e);
            throw new RuntimeException(e);
        }
    }

    public Long totalScriptInvocations() {
        QueryBuilder<DAppRelease, String> statementBuilder = dAppResultItemDao.queryBuilder();

        try {
            return statementBuilder.query()
                    .stream().map(DAppRelease::getScriptInvocationsCount).reduce(0L, Long::sum);
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
        var decomposedSortBy = repositoryColumnConverter.decomposeSortBy(sortBy);
        var decomposedSortOrder = repositoryColumnConverter.decomposeSortOrder(sortOrder);

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
