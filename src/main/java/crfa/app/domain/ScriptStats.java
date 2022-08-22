package crfa.app.domain;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;
import lombok.*;

@Builder
@Getter
@AllArgsConstructor
@NoArgsConstructor
@ToString
@DatabaseTable(tableName = "script_stats")
public class ScriptStats {

    @DatabaseField(id = true, columnName = "script_hash")
    private String scriptHash;

    @DatabaseField(canBeNull = false, columnName = "transaction_count")
    private Long transactionCount;

    @DatabaseField(canBeNull = false, columnName = "script_type")
    private ScriptType scriptType;

}
