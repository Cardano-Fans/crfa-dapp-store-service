package crfa.app.repository;

import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.dao.DaoManager;
import com.j256.ormlite.jdbc.JdbcConnectionSource;
import com.j256.ormlite.table.TableUtils;
import crfa.app.domain.*;
import io.micronaut.context.annotation.Value;
import io.micronaut.runtime.event.annotation.EventListener;
import io.micronaut.runtime.server.event.ServerStartupEvent;
import jakarta.inject.Singleton;
import lombok.extern.slf4j.Slf4j;

import java.sql.SQLException;

@Singleton
@Slf4j
public class DbManager {

    private JdbcConnectionSource connectionSource;

    @Value("${dbPath-dapps:crfa-dapps.db}")
    private String dbPath;

    private Dao<DAppRelease, String> dAppResultItemDao;
    private Dao<DApp, String> dAppDao;
    private Dao<ScriptStats, String> scriptsDao;
    private Dao<DAppReleaseItem, String> dappReleaseItemDao;

    private Dao<AdaPricePerDay, String> adaPricePerDayDao;

    @EventListener
    public void onStartup(ServerStartupEvent event) throws SQLException {
        log.info("Starting DbManager..., dbPath:{}", dbPath);

        String databaseUrl = String.format("jdbc:sqlite:%s", dbPath);
        // create a connection source to our database
        this.connectionSource = new JdbcConnectionSource(databaseUrl);

        this.dAppDao = DaoManager.createDao(connectionSource, DApp.class);
        this.scriptsDao = DaoManager.createDao(connectionSource, ScriptStats.class);
        this.dappReleaseItemDao = DaoManager.createDao(connectionSource, DAppReleaseItem.class);
        this.dAppResultItemDao = DaoManager.createDao(connectionSource, DAppRelease.class);
        this.adaPricePerDayDao = DaoManager.createDao(connectionSource, AdaPricePerDay.class);

        createDbsIfNecessary();
    }

    public Dao<DAppRelease, String> getdAppResultItemDao() {
        return dAppResultItemDao;
    }

    public Dao<DApp, String> getdAppDao() {
        return dAppDao;
    }

    public Dao<ScriptStats, String> getScriptsDao() {
        return scriptsDao;
    }

    public Dao<DAppReleaseItem, String> getDappReleaseItemDao() {
        return dappReleaseItemDao;
    }

    public Dao<AdaPricePerDay, String> getAdaPricePerDayDao() {
        return adaPricePerDayDao;
    }

    public <T> void clearDB(Class<T> clazz) {
        try {
            TableUtils.clearTable(connectionSource,  clazz);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    private void createDbsIfNecessary() throws SQLException {
        TableUtils.createTableIfNotExists(this.connectionSource, AdaPricePerDay.class);
        TableUtils.createTableIfNotExists(this.connectionSource, ScriptStats.class);
        TableUtils.createTableIfNotExists(this.connectionSource, DApp.class);
        TableUtils.createTableIfNotExists(this.connectionSource, DAppRelease.class);
        TableUtils.createTableIfNotExists(this.connectionSource, DAppReleaseItem.class);
    }

}
