package crfa.app.repository;

import crfa.app.domain.SortBy;
import crfa.app.domain.SortOrder;
import jakarta.inject.Singleton;

import java.util.Optional;

@Singleton
public class RepositoryColumnConverter {

    public String decomposeSortBy(Optional<SortBy> sortBy) {
        var sby = sortBy.orElse(SortBy.SCRIPTS_INVOKED);

        if (sby == SortBy.SCRIPTS_INVOKED) {
            return "script_invocations";
        }
        if (sby == SortBy.SCRIPTS_LOCKED) {
            return "scripts_locked";
        }
        if (sby == SortBy.TRANSACTIONS_COUNT) {
            return "trx_count";
        }
        if (sby == SortBy.TOTAL_VALUE_LOCKED) {
            return "total_value_locked";
        }
        if (sby == SortBy.FULL_NAME) {
            return "full_name";
        }

        return "trx_count";
    }

    public boolean decomposeSortOrder(Optional<SortOrder> sortOrder) {
        var so = sortOrder.orElse(SortOrder.DESC);

        if (so == SortOrder.ASC) {
            return true;
        }

        return false;
    }

}
