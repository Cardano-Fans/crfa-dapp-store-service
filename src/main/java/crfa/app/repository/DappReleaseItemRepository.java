package crfa.app.repository;

import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.dao.DaoManager;
import com.j256.ormlite.jdbc.JdbcConnectionSource;
import com.j256.ormlite.stmt.QueryBuilder;
import com.j256.ormlite.table.TableUtils;
import crfa.app.domain.DAppReleaseItem;
import io.micronaut.context.annotation.Value;
import io.micronaut.runtime.event.annotation.EventListener;
import io.micronaut.runtime.server.event.ServerStartupEvent;
import jakarta.inject.Singleton;
import lombok.extern.slf4j.Slf4j;

import java.sql.SQLException;
import java.util.List;

@Singleton
@Slf4j
public class DappReleaseItemRepository {

    private JdbcConnectionSource connectionSource;

    @Value("${dbPath:crfa-dapp-releases-db-dev.db}")
    private String dbPath;

    private Dao<DAppReleaseItem, String> dappReleaseItemDao;

    @EventListener
    public void onStartup(ServerStartupEvent event) throws SQLException {
        log.info("Starting DappReleaseItemRepository..., dbPath:{}", dbPath);

        String databaseUrl = String.format("jdbc:sqlite:%s", dbPath);
        // create a connection source to our database
        this.connectionSource = new JdbcConnectionSource(databaseUrl);

        this.dappReleaseItemDao = DaoManager.createDao(connectionSource, DAppReleaseItem.class);

        createDbsIfNecessary();
    }

    public List<DAppReleaseItem> listReleaseItems(String releaseKey) {
        try {
            QueryBuilder<DAppReleaseItem, String> statementBuilder = dappReleaseItemDao.queryBuilder();

            return statementBuilder
                    .where().eq("release_key", releaseKey)
                    .query();
        } catch (SQLException e) {
            log.error("db error", e);
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
