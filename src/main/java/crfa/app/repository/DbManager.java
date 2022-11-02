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
import lombok.val;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.function.Supplier;

@Singleton
@Slf4j
public class DbManager {

    private JdbcConnectionSource connectionSource;

    @Value("${dbPath-dapps:crfa-dapps.db}")
    private String dbPath;

    private Dao<DAppRelease, String> dAppReleaseDao;

    private Dao<DAppReleaseEpoch, String> dAppReleaseEpochDao;

    private Dao<DApp, String> dAppDao;
    private Dao<DAppEpoch, String> dAppEpochDao;

    private Dao<DappScriptItem, String> dappScriptItems;
    private Dao<DappScriptItemEpoch, String> dappScriptItemEpochs;

    private Dao<AdaPricePerDay, String> adaPricePerDayDao;
    private Dao<ScriptStats, String> scriptsDao;

    private Dao<GlobalStats, String> globalStatsDao;
    private Dao<GlobalStatsEpoch, Integer> globalStatsEpochDao;

    private Dao<GlobalCategoryStats, String> globalCategoryStatsDao;
    private Dao<GlobalCategoryEpochStats, String> globalCategoryStatsEpochDao;

    private Dao<Pool, String> poolDao;

    @EventListener
    public void onStartup(ServerStartupEvent event) throws SQLException {
        log.info("Starting DbManager..., dbPath:{}", dbPath);

        String databaseUrl = String.format("jdbc:sqlite:%s", dbPath);
        //String databaseUrl = String.format("jdbc:h2:mem:");
        // create a connection source to our database
        this.connectionSource = new JdbcConnectionSource(databaseUrl);

        this.dAppDao = DaoManager.createDao(connectionSource, DApp.class);
        this.dAppEpochDao = DaoManager.createDao(connectionSource, DAppEpoch.class);

        this.dappScriptItems = DaoManager.createDao(connectionSource, DappScriptItem.class);
        this.dappScriptItemEpochs = DaoManager.createDao(connectionSource, DappScriptItemEpoch.class);

        this.dAppReleaseDao = DaoManager.createDao(connectionSource, DAppRelease.class);
        this.dAppReleaseEpochDao = DaoManager.createDao(connectionSource, DAppReleaseEpoch.class);

        this.adaPricePerDayDao = DaoManager.createDao(connectionSource, AdaPricePerDay.class);
        this.scriptsDao = DaoManager.createDao(connectionSource, ScriptStats.class);

        this.globalStatsDao = DaoManager.createDao(connectionSource, GlobalStats.class);
        this.globalStatsEpochDao = DaoManager.createDao(connectionSource, GlobalStatsEpoch.class);

        this.globalCategoryStatsDao = DaoManager.createDao(connectionSource, GlobalCategoryStats.class);
        this.globalCategoryStatsEpochDao = DaoManager.createDao(connectionSource, GlobalCategoryEpochStats.class);

        this.poolDao = DaoManager.createDao(connectionSource, Pool.class);

        createDbsIfNecessary();
    }

    public Dao<DAppRelease, String> getdAppReleasesDao() {
        return dAppReleaseDao;
    }

    public Dao<DAppReleaseEpoch, String> getdAppReleaseEpochDao() {
        return dAppReleaseEpochDao;
    }

    public Dao<DApp, String> getdAppDao() {
        return dAppDao;
    }

    public Dao<DAppEpoch, String> getdAppEpochDao() {
        return dAppEpochDao;
    }

    public Dao<ScriptStats, String> getScriptsStatsDao() {
        return scriptsDao;
    }

    public Dao<DappScriptItem, String> getDappScriptItems() {
        return dappScriptItems;
    }

    public Dao<DappScriptItemEpoch, String> getDappScriptItemEpochs() {
        return dappScriptItemEpochs;
    }

    public Dao<AdaPricePerDay, String> getAdaPricePerDayDao() {
        return adaPricePerDayDao;
    }

    public Dao<GlobalStats, String> getGlobalStatsDao() {
        return globalStatsDao;
    }

    public Dao<GlobalCategoryStats, String> getGlobalCategoryStatsDao() {
        return globalCategoryStatsDao;
    }

    public Dao<GlobalCategoryEpochStats, String> getGlobalCategoryStatsEpochDao() {
        return globalCategoryStatsEpochDao;
    }

    public Dao<GlobalStatsEpoch, Integer> getGlobalStatsEpochDao() {
        return globalStatsEpochDao;
    }

    public Dao<Pool, String> getPoolDao() {
        return poolDao;
    }

    public <T> void clearDB(Class<T> clazz) {
        try {
            TableUtils.clearTable(connectionSource,  clazz);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    private void createDbsIfNecessary() throws SQLException {
        TableUtils.createTableIfNotExists(this.connectionSource, DApp.class);
        TableUtils.createTableIfNotExists(this.connectionSource, DAppEpoch.class);

        TableUtils.createTableIfNotExists(this.connectionSource, DAppRelease.class);
        TableUtils.createTableIfNotExists(this.connectionSource, DAppReleaseEpoch.class);

        TableUtils.createTableIfNotExists(this.connectionSource, DappScriptItem.class);
        TableUtils.createTableIfNotExists(this.connectionSource, DappScriptItemEpoch.class);

        TableUtils.createTableIfNotExists(this.connectionSource, AdaPricePerDay.class);
        TableUtils.createTableIfNotExists(this.connectionSource, ScriptStats.class);

        TableUtils.createTableIfNotExists(this.connectionSource, GlobalStats.class);
        TableUtils.createTableIfNotExists(this.connectionSource, GlobalStatsEpoch.class);

        TableUtils.createTableIfNotExists(this.connectionSource, GlobalCategoryStats.class);
        TableUtils.createTableIfNotExists(this.connectionSource, GlobalCategoryEpochStats.class);

        TableUtils.createTableIfNotExists(this.connectionSource, Pool.class);
    }

    // TODO optimise this by moving responsibility to db engine via SQL delete statement
    public <T> void removeAllExcept(Collection<T> items, Supplier<Dao<T, String>> supplier) {
        log.info("removeAllExcept, itemsCount:{}", items.size());

        var allLiveIds = new ArrayList<String>();

        val dao = supplier.get();

        dao.forEach(item -> {
            try {
                allLiveIds.add(dao.extractId(item));
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
        });

        items.forEach(item -> {
            try {
                val id = dao.extractId(item);
                if (!allLiveIds.contains(id)) {
                    log.info("Clearing dangling item from db, id:{}", id);
                    dao.deleteById(id);
                }
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
        });

        log.info("removeAllExcept done.");
    }

}
