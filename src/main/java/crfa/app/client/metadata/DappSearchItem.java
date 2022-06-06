package crfa.app.client.metadata;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.*;

import java.util.List;

@Builder
@Getter
@ToString
@Setter
@AllArgsConstructor
@JsonIgnoreProperties
@NoArgsConstructor
public class DappSearchItem {

    String id;

    String name;

    String category;

    String subCategory;

    String url;

    String twitter;

    String icon;

    String type;

    List<DappReleaseItem> releases;

}
