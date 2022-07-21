package crfa.app.repository;

import crfa.app.domain.SortBy;
import crfa.app.domain.SortOrder;
import jakarta.inject.Singleton;

import java.util.Optional;

@Singleton
public class RepositoryColumnConverter {

    private static final SortBy DEFAULT_SORT_BY = SortBy.SCRIPTS_INVOKED;
    private static final SortOrder DEFAULT_SORT_ORDER = SortOrder.DESC;

    public Optional<String> decomposeSortBy(Optional<SortBy> sortBy) {
        if (sortBy.isEmpty()) {
            return decomposeSortBy(Optional.of(DEFAULT_SORT_BY));
        }

        return sortBy.flatMap(sby -> {
            if (sby == SortBy.SCRIPTS_INVOKED) {
                return Optional.of("script_invocations");
            }
            if (sby == SortBy.SCRIPTS_LOCKED) {
                return Optional.of("scripts_locked");
            }
            if (sby == SortBy.TRANSACTIONS_COUNT) {
                return Optional.of("trx_count");
            }
            if (sby == SortBy.TOTAL_VALUE_LOCKED) {
                return Optional.of("total_value_locked");
            }
            if (sby == SortBy.FULL_NAME) {
                return Optional.of("full_name");
            }

            if (sby == SortBy.RELEASE_NUMBER) {
                return Optional.of("release_number");
            }

            return decomposeSortBy(Optional.of(DEFAULT_SORT_BY));
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
