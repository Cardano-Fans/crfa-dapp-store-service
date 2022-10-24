package crfa.app.repository;

import crfa.app.domain.SortBy;
import crfa.app.domain.SortOrder;
import jakarta.inject.Singleton;

import java.util.ArrayList;
import java.util.List;

import static crfa.app.domain.SortBy.*;

@Singleton
public class RepositoryColumnConverter {

    public List<String> decomposeSortBy(SortBy sby) {
        var columnNames = new ArrayList<String>();

        if (sby == SCRIPTS_INVOKED) {
            columnNames.add("transactions");
        }
        if (sby == SCRIPTS_LOCKED) {
            columnNames.add("balance");
        }
        if (sby == FULL_NAME) {
            columnNames.add("full_name");
        }
        if (sby == RELEASE_NUMBER) {
            columnNames.add("release_number");
        }

        return columnNames;
    }

    public Boolean decomposeSortOrder(SortOrder so) {
        return so == SortOrder.ASC;
    }

}
