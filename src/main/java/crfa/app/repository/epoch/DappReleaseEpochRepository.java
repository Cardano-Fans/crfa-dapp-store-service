package crfa.app.repository.epoch;

import com.j256.ormlite.stmt.QueryBuilder;
import crfa.app.domain.DAppReleaseEpoch;
import crfa.app.repository.DbManager;
import crfa.app.repository.RepositoryColumnConverter;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import lombok.extern.slf4j.Slf4j;

import java.sql.SQLException;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

@Singleton
@Slf4j
public class DappReleaseEpochRepository {

    @Inject
    private DbManager dbManager;

    @Inject
    private RepositoryColumnConverter repositoryColumnConverter;

    public float getMaxReleaseVersion(String id) {
        QueryBuilder<DAppReleaseEpoch, String> statementBuilder = dbManager.getdAppReleaseEpochDao().queryBuilder();

        try {
            return Optional.ofNullable(statementBuilder
                    .selectColumns("release_number")
                    .orderBy("release_number", false)
                    .limit(1L)
                    .where().eq("id", id)
                    .queryForFirst())
                    .map(DAppReleaseEpoch::getReleaseNumber)
                    .orElse(-1.0f);
        } catch (SQLException e) {
            log.error("db error", e);
            throw new RuntimeException(e);
        }
    }

    public List<DAppReleaseEpoch> findByReleaseKey(String releaseKey) {
        try {
            QueryBuilder<DAppReleaseEpoch, String> statementBuilder = dbManager.getdAppReleaseEpochDao().queryBuilder();

            statementBuilder
                    .orderBy("epoch_no", false)
                    .where().eq("key", releaseKey);

            return dbManager.getdAppReleaseEpochDao().query(statementBuilder.prepare()).stream().toList();
        } catch (SQLException e) {
            log.error("db error", e);
            throw new RuntimeException(e);
        }
    }

    public void upsertDAppRelease(DAppReleaseEpoch dAppRelease) {
        try {
            dbManager.getdAppReleaseEpochDao().createOrUpdate(dAppRelease);
        } catch (SQLException e) {
            log.error("db error", e);
            throw new RuntimeException(e);
        }
    }

    public void removeAllExcept(Collection<DAppReleaseEpoch> items) {
        dbManager.removeAllExcept(items, () -> dbManager.getdAppReleaseEpochDao());
    }

}
