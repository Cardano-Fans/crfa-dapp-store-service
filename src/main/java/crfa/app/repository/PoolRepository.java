package crfa.app.repository;

import crfa.app.domain.Pool;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import lombok.extern.slf4j.Slf4j;

import java.sql.SQLException;
import java.util.Optional;

@Singleton
@Slf4j
public class PoolRepository {

    @Inject
    private DbManager dbManager;

    public long poolCount() {
        try {
            return dbManager.getPoolDao().countOf();
        } catch (SQLException e) {
            log.error("db error", e);
            throw new RuntimeException(e);
        }
    }

    public Optional<Pool> findById(String id) {
        try {
            return Optional.ofNullable(dbManager.getPoolDao().queryForId(id));
        } catch (SQLException e) {
            log.error("db error", e);
            throw new RuntimeException(e);
        }
    }

    public void upsert(Pool pool) {
        try {
            dbManager.getPoolDao().createOrUpdate(pool);
        } catch (SQLException e) {
            log.error("db error", e);
            throw new RuntimeException(e);
        }
    }

}
