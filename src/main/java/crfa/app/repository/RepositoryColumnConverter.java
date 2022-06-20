package crfa.app.repository;

import crfa.app.domain.SortBy;
import crfa.app.domain.SortOrder;
import jakarta.inject.Singleton;

import java.util.Optional;

@Singleton
public class RepositoryColumnConverter {

    public Optional<String> decomposeSortBy(Optional<SortBy> sortBy) {
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

            return Optional.empty();
        });
    }

    public Optional<Boolean> decomposeSortOrder(Optional<SortOrder> sortOrder) {
        return sortOrder.flatMap(so -> {
            if (so == SortOrder.ASC) {
                return Optional.of(true);
            }

            return Optional.of(false);
        });
    }

}
