package crfa.app.resource;

import crfa.app.domain.ScriptStats;
import crfa.app.repository.total.ScriptHashesStatsRepository;
import io.micronaut.http.annotation.Controller;
import io.micronaut.http.annotation.Get;
import jakarta.inject.Inject;
import lombok.extern.slf4j.Slf4j;

import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.Map;

import static crfa.app.domain.ScriptStatsType.DB_SYNC;
import static java.util.stream.Collectors.toMap;

@Controller("/internal")
@Slf4j
public class InternalResource {

    @Inject
    private ScriptHashesStatsRepository scriptHashesStatsRepository;

    @Get(uri = "/scriptStats", produces = "application/json")
    public Map<String, Long> scriptStats() {
        return scriptHashesStatsRepository.listScriptStatsOrderedByTransactionCount(DB_SYNC)
                .stream()
                .collect(toMap(ScriptStats::getScriptHash, ScriptStats::getCount))
                .entrySet()
                .stream()
                .sorted(Map.Entry.comparingByValue(Comparator.reverseOrder()))
                .filter(entry -> entry.getValue() >= 5L)
                .collect(toMap(Map.Entry::getKey, Map.Entry::getValue, (e1, e2) -> e1, LinkedHashMap::new));
    }

}
