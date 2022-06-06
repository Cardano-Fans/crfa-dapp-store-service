package crfa.app.domain;

import com.j256.ormlite.field.DataType;
import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;
import lombok.*;

import java.util.Date;

@Builder
@DatabaseTable(tableName = "ada_price_per_day")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class AdaPricePerDay {

    @DatabaseField(id = true, canBeNull = false, index = true)
    String key;

    @DatabaseField(canBeNull = false)
    float price;

    @DatabaseField(canBeNull = false, index = true)
    String currency;

    @DatabaseField(canBeNull = false, index = true, dataType = DataType.DATE_STRING)
    Date modDate;

}
