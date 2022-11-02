package crfa.app.domain;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;
import lombok.*;

import javax.annotation.Nullable;

@DatabaseTable(tableName = "pool")
@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class Pool {

    @DatabaseField(index = true, id = true, canBeNull = false, columnName = "id")
    private String hex;

    @DatabaseField(index = true)
    private String bech32;

    @DatabaseField
    @Nullable private String ticker;

}
