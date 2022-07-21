//package crfa.app.service;
//
//import crfa.app.domain.DAppRelease;
//import crfa.app.domain.DAppReleaseItem;
//import crfa.app.domain.SortBy;
//import crfa.app.domain.SortOrder;
//import crfa.app.repository.DappReleaseItemRepository;
//import crfa.app.resource.InvalidParameterException;
//import jakarta.inject.Singleton;
//import lombok.AllArgsConstructor;
//import lombok.Getter;
//import lombok.extern.slf4j.Slf4j;
//
//import java.util.*;
//import java.util.stream.Collectors;
//
//@Singleton
//@Slf4j
//public class DappReleasesService {
//
//    private DappReleaseItemRepository dappReleaseItemRepository;
//
//    public List<DAppRelease> listDapps(Optional<SortBy> sortBy, Optional<SortOrder> sortOrder) throws InvalidParameterException {
//        var items = dappReleaseItemRepository.listReleaseItems(sortBy, sortOrder);
//
//        var dappsGrouped = items.stream().collect(Collectors.groupingBy(DAppReleaseItem::getDappId));
//
//        var totalScriptInvocations = dappsGrouped.entrySet().stream()
//                .collect(Collectors.toMap(Map.Entry::getKey, e -> {
//                    var scriptInvocations = e.getValue().stream().map(DAppReleaseItem::getScriptInvocationsCount).reduce(0L, Long::sum);
//                    var scriptsLocked = e.getValue().stream().map(DAppReleaseItem::getScriptsLocked).reduce(0L, (a, b) -> a + (b == null ? 0 : b));
//                    var transactionsCount = e.getValue().stream().map(DAppReleaseItem::getTransactionsCount).reduce(0L, (a, b) -> a + (b == null ? 0 : b));
//
//                    return new TotalCounts(transactionsCount, scriptInvocations, scriptsLocked);
//                }));
//
//        var m = new LinkedHashMap<String, DAppRelease>();
//
//        items.forEach(dAppReleaseItem -> {
//
//            var dappRelease = DAppRelease.builder()
//                    .releaseName(dAppReleaseItem.getDappReleaseName())
//                    .releaseNumber(dAppReleaseItem.getDappReleaseNumber())
//                    .fullName(dAppReleaseItem.getDappFullName())
//                    .id(dAppReleaseItem.getDappId())
//                    .dAppType()
//
//        });
//    }
//
//    @Getter
//    @AllArgsConstructor
//    private static class TotalCounts {
//
//        long totalTransactions;
//        long scriptInvocations;
//        long scriptsLocked;
//
//    }
//
//}
