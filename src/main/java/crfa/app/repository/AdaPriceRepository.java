package crfa.app.repository;

import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.dao.DaoManager;
import com.j256.ormlite.jdbc.JdbcConnectionSource;
import com.j256.ormlite.stmt.QueryBuilder;
import com.j256.ormlite.table.TableUtils;
import crfa.app.domain.AdaPricePerDay;
import io.micronaut.context.annotation.Value;
import io.micronaut.runtime.event.annotation.EventListener;
import io.micronaut.runtime.server.event.ServerStartupEvent;
import jakarta.inject.Singleton;
import lombok.extern.slf4j.Slf4j;

import java.sql.SQLException;
import java.util.Optional;

@Singleton
@Slf4j
public class AdaPriceRepository {

    private JdbcConnectionSource connectionSource;

    @Value("${dbPath-dapps-ada-price:crfa-cardano-dapp-store-ada-price.db}")
    private String dbPath;

    private Dao<AdaPricePerDay, String> adaPricePerDayDao;

    @EventListener
    public void onStartup(ServerStartupEvent event) throws SQLException {
        log.info("Starting DappResultItemRepository..., dbPath:{}", dbPath);

        String databaseUrl = String.format("jdbc:sqlite:%s", dbPath);
        // create a connection source to our database
        this.connectionSource = new JdbcConnectionSource(databaseUrl);

        this.adaPricePerDayDao = DaoManager.createDao(connectionSource, AdaPricePerDay.class);

        createDbsIfNecessary();
    }

    public Optional<AdaPricePerDay> getLatestPrice(String currency) {
        try {
            QueryBuilder<AdaPricePerDay, String> statementBuilder = adaPricePerDayDao.queryBuilder();

            statementBuilder.orderBy("moddate", false);

            return statementBuilder
                    .where().eq("currency", currency.toUpperCase())
                    .query()
                    .stream().findFirst();
        } catch (SQLException e) {
            log.error("db error", e);
            throw new RuntimeException(e);
        }
    }

    public void updatePrice(AdaPricePerDay adaPricePerDay) {
        try {
            adaPricePerDayDao.createOrUpdate(adaPricePerDay);
        } catch (SQLException e) {
            log.error("db error", e);
            throw new RuntimeException(e);
        }
    }

    public void createDbsIfNecessary() throws SQLException {
        TableUtils.createTableIfNotExists(this.connectionSource, AdaPricePerDay.class);
    }

}
