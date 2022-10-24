package crfa.app.repository.epoch;

import com.j256.ormlite.stmt.QueryBuilder;
import crfa.app.domain.DappScriptItemEpoch;
import crfa.app.repository.DbManager;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import lombok.extern.slf4j.Slf4j;

import java.sql.SQLException;
import java.util.Collection;
import java.util.List;

@Singleton
@Slf4j
public class DappScriptsEpochRepository {

    @Inject
    private DbManager dbManager;

    public List<DappScriptItemEpoch> listByHash(String hash) {
        try {
            QueryBuilder<DappScriptItemEpoch, String> statementBuilder = dbManager.getDappScriptItemEpochs().queryBuilder();

            return statementBuilder
                    .where()
                    .eq("hash", hash)
                    .query();
        } catch (SQLException e) {
            log.error("db error", e);
            throw new RuntimeException(e);
        }
    }

    public void update(DappScriptItemEpoch dappScriptItem) {
        try {
            dbManager.getDappScriptItemEpochs().createOrUpdate(dappScriptItem);
        } catch (SQLException e) {
            log.error("db error", e);
            throw new RuntimeException(e);
        }
    }

    public void removeAllExcept(Collection<DappScriptItemEpoch> dappScriptItems) {
        dbManager.removeAllExcept(dappScriptItems, () -> dbManager.getDappScriptItemEpochs());
    }

    public void closeEpochs(int currentEpochNo) {
        try {
            dbManager.getDappScriptItemEpochs().executeRaw("UPDATE dapp_script_item_epoch SET closed_epoch = true WHERE epoch_no < " + currentEpochNo);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

}
