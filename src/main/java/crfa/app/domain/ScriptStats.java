package crfa.app.domain;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;
import lombok.*;

// table used for stats for /internal/ endpoint

@Builder
@Getter
@AllArgsConstructor
@NoArgsConstructor
@ToString
@DatabaseTable(tableName = "script_stats")
public class ScriptStats {

    @DatabaseField(id = true, columnName = "script_hash")
    private String scriptHash;

    @DatabaseField(canBeNull = false, columnName = "count")
    private Long count;

    @DatabaseField(canBeNull = false, columnName = "script_type", index = true)
    private ScriptType scriptType;

    @DatabaseField(canBeNull = false, columnName = "type", index = true)
    private ScriptStatsType type;

}
