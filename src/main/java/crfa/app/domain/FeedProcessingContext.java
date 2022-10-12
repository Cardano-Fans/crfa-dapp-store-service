package crfa.app.domain;

import lombok.Builder;
import lombok.Getter;

import java.util.Map;
import java.util.Set;

@Builder
@Getter
public class FeedProcessingContext {

    Set<String> uniqueAccounts;

    Map<Integer, Set<String>> uniqueAccountsEpoch;

}
