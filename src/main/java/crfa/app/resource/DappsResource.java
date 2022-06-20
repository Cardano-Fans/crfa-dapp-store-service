package crfa.app.resource;

import crfa.app.domain.DAppRelease;
import crfa.app.domain.SortBy;
import crfa.app.domain.SortOrder;
import crfa.app.repository.DappReleaseItemRepository;
import crfa.app.repository.DappReleasesRepository;
import io.micronaut.http.annotation.Controller;
import io.micronaut.http.annotation.Get;
import io.micronaut.http.annotation.PathVariable;
import io.micronaut.http.annotation.QueryValue;
import jakarta.inject.Inject;
import lombok.extern.slf4j.Slf4j;

import java.util.List;
import java.util.Optional;

@Controller("/dapps")
@Slf4j
public class DappsResource {

    @Inject
    private DappReleasesRepository dappReleasesRepository;

    @Inject
    private DappReleaseItemRepository dappReleaseItemRepository;

    @Get(uri = "/list-releases", produces = "application/json")
    public List<DAppRelease> listDappReleases(@QueryValue Optional<SortBy> sortBy,
                                              @QueryValue Optional<SortOrder> sortOrder) throws InvalidParameterException {
        return dappReleasesRepository.listDapps(sortBy, sortOrder);
    }

    @Get(uri = "/by-release-key/{releaseKey}", produces = "application/json")
    public DappScriptsResponse listScriptsResponse(@PathVariable String releaseKey,
                                                   @QueryValue Optional<SortBy> sortBy,
                                                   @QueryValue Optional<SortOrder> sortOrder) throws DappReleaseNotFoundException, InvalidParameterException {
        var maybeDappRelease = dappReleasesRepository.findByReleaseKey(releaseKey);

        if (maybeDappRelease.isEmpty()) {
            throw new DappReleaseNotFoundException("Dapp release key not found: " + releaseKey);
        }

        var dappRelease = maybeDappRelease.get();

        var releaseItems = dappReleaseItemRepository.listReleaseItems(releaseKey, sortBy, sortOrder);

        return DappScriptsResponse.builder()
                .release(dappRelease)
                .scripts(releaseItems)
                .build();
    }

}
