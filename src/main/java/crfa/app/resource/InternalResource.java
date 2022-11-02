package crfa.app.resource;

import crfa.app.domain.InjestionMode;
import crfa.app.domain.ScriptStats;
import crfa.app.domain.ScriptStatsType;
import crfa.app.domain.ScriptType;
import crfa.app.repository.total.ScriptHashesStatsRepository;
import crfa.app.service.DappFeedCreator;
import crfa.app.service.DappIngestionService;
import crfa.app.service.PoolService;
import crfa.app.service.ScriptHashesService;
import io.micronaut.http.HttpResponse;
import io.micronaut.http.HttpStatus;
import io.micronaut.http.annotation.Controller;
import io.micronaut.http.annotation.Get;
import io.micronaut.http.annotation.Post;
import jakarta.inject.Inject;
import lombok.extern.slf4j.Slf4j;
import lombok.val;

import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.Map;

import static java.util.stream.Collectors.toMap;

@Controller("/internal")
@Slf4j
public class InternalResource {

    @Inject
    private ScriptHashesStatsRepository scriptHashesStatsRepository;

    @Inject
    private DappIngestionService dappIngestionService;

    @Inject
    private DappFeedCreator dappFeedCreator;

    @Inject
    private ScriptHashesService scriptHashesService;

    @Inject
    private PoolService poolService;

    @Get(uri = "/scriptStats/{type}/{scriptType}", produces = "application/json")
    public Map<String, Long> scriptStats(ScriptStatsType type, ScriptType scriptType) {
        return scriptHashesStatsRepository.listScriptStatsOrderedByTransactionCount(type, scriptType)
                .stream()
                .collect(toMap(ScriptStats::getScriptHash, ScriptStats::getCount))
                .entrySet()
                .stream()
                .sorted(Map.Entry.comparingByValue(Comparator.reverseOrder()))
                .filter(entry -> entry.getValue() >= 5L)
                .collect(toMap(Map.Entry::getKey, Map.Entry::getValue, (e1, e2) -> e1, LinkedHashMap::new));
    }

    @Post(value = "/rebuildDb", consumes = "application/json", produces = "application/json")
    public HttpResponse<?> rebuildDb() {
        val injestionMode = InjestionMode.FULL;

        log.info("Dapps update scheduled, mode:{}", injestionMode);

        log.info("Gathering data feed...");
        val dataFeed = dappFeedCreator.createFeed(injestionMode);
        log.info("Got data feed.");

        dappIngestionService.process(dataFeed, injestionMode);

        log.info("dapps update completed, mode:{}", injestionMode);

        return HttpResponse.status(HttpStatus.OK);
    }

    @Post(value = "/rebuildDbPartially", consumes = "application/json", produces = "application/json")
    public HttpResponse<?> rebuildDbPartial() {
        val injestionMode = InjestionMode.WITHOUT_EPOCHS_ONLY_AGGREGATES;

        log.info("Dapps update scheduled, mode:{}", injestionMode);

        log.info("Gathering data feed...");
        val dataFeed = dappFeedCreator.createFeed(injestionMode);
        log.info("Got data feed.");

        dappIngestionService.process(dataFeed, injestionMode);

        log.info("dapps update completed, mode:{}", injestionMode);

        return HttpResponse.status(HttpStatus.OK);
    }

    @Post(value = "/rebuildScriptStatsDb", consumes = "application/json", produces = "application/json")
    public HttpResponse<?> rebuildScriptStats() {
        log.info("rebuilding script stats...");
        scriptHashesService.ingestAll();
        log.info("rebuilding script stats completed.");

        return HttpResponse.status(HttpStatus.OK);
    }

    @Post(value = "/rebuildPoolsDb", consumes = "application/json", produces = "application/json")
    public HttpResponse<?> rebuildPoolsDb() {
        log.info("rebuilding pools db...");
        poolService.updatePools();
        log.info("rebuilding pools db completed.");

        return HttpResponse.status(HttpStatus.OK);
    }

}
