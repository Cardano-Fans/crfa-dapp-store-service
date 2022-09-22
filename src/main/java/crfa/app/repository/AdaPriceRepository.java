package crfa.app.repository;

import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.stmt.QueryBuilder;
import crfa.app.domain.AdaPricePerDay;
import io.micronaut.runtime.event.annotation.EventListener;
import io.micronaut.runtime.server.event.ServerStartupEvent;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import lombok.extern.slf4j.Slf4j;

import java.sql.SQLException;
import java.util.Optional;

@Singleton
@Slf4j
public class AdaPriceRepository {

    @Inject
    private DbManager dbManager;

    public Optional<AdaPricePerDay> getLatestPrice(String currency) {
        try {
            QueryBuilder<AdaPricePerDay, String> statementBuilder = dbManager.getAdaPricePerDayDao().queryBuilder();

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
            dbManager.getAdaPricePerDayDao().createOrUpdate(adaPricePerDay);
        } catch (SQLException e) {
            log.error("db error", e);
            throw new RuntimeException(e);
        }
    }

}
