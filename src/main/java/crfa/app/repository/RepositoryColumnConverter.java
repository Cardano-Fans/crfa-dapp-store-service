package crfa.app.repository;

import crfa.app.domain.DappAggrType;
import crfa.app.domain.SortBy;
import crfa.app.domain.SortOrder;
import jakarta.inject.Singleton;

import java.util.List;
import java.util.Optional;

import static crfa.app.domain.DappAggrType.LAST;
import static crfa.app.domain.SortBy.*;

@Singleton
public class RepositoryColumnConverter {

    private static final SortBy DEFAULT_SORT_BY = SCRIPTS_INVOKED;
    private static final SortOrder DEFAULT_SORT_ORDER = SortOrder.DESC;

    public Optional<String> decomposeSortBy(Optional<SortBy> sortBy) {
        return decomposeSortBy(sortBy, DappAggrType.def());
    }

    public Optional<String> decomposeSortBy(Optional<SortBy> sortBy, DappAggrType dappAggrType) {
        if (sortBy.isEmpty()) {
            return decomposeSortBy(Optional.of(DEFAULT_SORT_BY), dappAggrType);
        }

        return sortBy.flatMap(sby -> {
            var columnName = "";

            if (sby == SCRIPTS_INVOKED) {
                columnName = "script_invocations";
            }
            if (sby == SCRIPTS_LOCKED) {
                columnName = "scripts_locked";
            }
            if (sby == TRANSACTIONS_COUNT) {
                columnName = "trx_count";
            }
            if (sby == FULL_NAME) {
                columnName = "full_name";
            }
            if (sby == RELEASE_NUMBER) {
                columnName = "release_number";
            }

            if (List.of(SCRIPTS_INVOKED, SCRIPTS_LOCKED, TRANSACTIONS_COUNT).contains(sby) && dappAggrType == LAST) {
                columnName = "last_version_" + columnName;
            }

            if (!columnName.isBlank()) {
                return Optional.of(columnName);
            }

            return decomposeSortBy(Optional.of(DEFAULT_SORT_BY), dappAggrType);
        });
    }

    public Optional<Boolean> decomposeSortOrder(Optional<SortOrder> sortOrder) {
        if (sortOrder.isEmpty()) {
            return decomposeSortOrder(Optional.of(DEFAULT_SORT_ORDER));
        }

        return sortOrder.flatMap(so -> {
            if (so == SortOrder.ASC) {
                return Optional.of(true);
            }

            return Optional.of(false);
        });
    }

}
