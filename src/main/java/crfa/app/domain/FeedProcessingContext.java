package crfa.app.domain;

import lombok.Builder;
import lombok.Getter;

import java.util.Set;

@Builder
@Getter
public class FeedProcessingContext {

    Set<String> uniqueAccounts;

}
