package crfa.app.repository.total;

import com.j256.ormlite.stmt.QueryBuilder;
import crfa.app.domain.DAppRelease;
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
                    .where().eq("dapp_id", id)
                    .queryForFirst())
                    .map(DAppRelease::getReleaseNumber)
                    .orElse(-1.0f);
        } catch (SQLException e) {
            log.error("db error", e);
            throw new RuntimeException(e);
        }
    }

    public Optional<DAppRelease> findById(String id) {
        try {
            val statementBuilder = dbManager.getdAppReleasesDao().queryBuilder();

            statementBuilder
                    .where().eq("id", id);

            return dbManager.getdAppReleasesDao().query(statementBuilder.prepare()).stream().findFirst();
        } catch (SQLException e) {
            log.error("db error", e);
            throw new RuntimeException(e);
        }
    }

    public List<DAppRelease> listDappReleases(SortBy sortBy, SortOrder sortOrder) {
        val decomposedSortBy = repositoryColumnConverter.decomposeSortBy(sortBy);
        val decomposedSortOrder = repositoryColumnConverter.decomposeSortOrder(sortOrder);

        try {
            val statementBuilder = dbManager.getdAppReleasesDao().queryBuilder();
            decomposedSortBy.forEach(column -> statementBuilder.orderBy(column, decomposedSortOrder));

            return statementBuilder.query();
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
