package crfa.app.repository;

import com.j256.ormlite.stmt.QueryBuilder;
import crfa.app.domain.DAppRelease;
import crfa.app.domain.SortBy;
import crfa.app.domain.SortOrder;
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
public class DappReleaseRepository {

    @Inject
    private DbManager dbManager;

    @Inject
    private RepositoryColumnConverter repositoryColumnConverter;

    public float getMaxReleaseVersion(String id) {
        QueryBuilder<DAppRelease, String> statementBuilder = dbManager.getdAppReleasesDao().queryBuilder();

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
            QueryBuilder<DAppRelease, String> statementBuilder = dbManager.getdAppReleasesDao().queryBuilder();

            statementBuilder
                    .where().eq("key", releaseKey);

            return dbManager.getdAppReleasesDao().query(statementBuilder.prepare()).stream().findFirst();
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
            QueryBuilder<DAppRelease, String> statementBuilder = dbManager.getdAppReleasesDao().queryBuilder();

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
            dbManager.getdAppReleasesDao().createOrUpdate(dAppRelease);
        } catch (SQLException e) {
            log.error("db error", e);
            throw new RuntimeException(e);
        }
    }

    public void removeAllExcept(Collection<DAppRelease> items) {
        dbManager.removeAllExcept(items, () -> dbManager.getdAppReleasesDao());
    }

}
