package crfa.app.repository;

import crfa.app.domain.SortBy;
import crfa.app.domain.SortOrder;
import jakarta.inject.Singleton;

import static crfa.app.domain.SortBy.*;

@Singleton
public class RepositoryColumnConverter {

    public String decomposeSortBy(SortBy sby) {
        var columnName = "";

        if (sby == SCRIPTS_INVOKED) {
            columnName = "script_invocations";
        }
        if (sby == SCRIPTS_LOCKED) {
            columnName = "scripts_locked";
        }
        if (sby == FULL_NAME) {
            columnName = "full_name";
        }
        if (sby == RELEASE_NUMBER) {
            columnName = "release_number";
        }

        if (!columnName.isBlank()) {
            return columnName;
        }

        throw new RuntimeException("mapping failed");
    }

    public Boolean decomposeSortOrder(SortOrder so) {
        return so == SortOrder.ASC ? true : false;
    }

}
