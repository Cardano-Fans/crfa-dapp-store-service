package crfa.app.service;

import crfa.app.domain.EpochKey;
import crfa.app.domain.Eras;
import crfa.app.domain.PoolError;
import crfa.app.domain.SnapshotType;
import io.vavr.control.Either;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.commons.lang3.StringUtils;
import org.redisson.api.RedissonClient;
import org.redisson.client.codec.StringCodec;

import java.util.*;
import java.util.stream.Collectors;

import static crfa.app.domain.SnapshotType.ALL;
import static java.lang.Integer.MAX_VALUE;
import static java.lang.String.format;

@Singleton
@Slf4j
public class ScrollsOnChainDataService {

    public static final int ONE_MLN = 1_000_000;

    @Inject
    private RedissonClient redissonClient;

    public Map<String, Long> mintTransactionsCount(Collection<String> mintPolicyIds) {
        log.info("loading mint policy ids counts...");

        val m = new HashMap<String, Long>();

        val collection = "c5";

        mintPolicyIds.forEach(hash -> {
            val key = String.format("%s.%s", collection, hash);

            val c = redissonClient.getAtomicLong(key);

            if (c.isExists()) {
                val value = c.get();

                m.put(hash, value);
            } else {
                m.put(hash, 0L);
            }
        });

        return m;
    }

    public Map<EpochKey<String>, Long> mintTransactionsCountWithEpoch(Collection<String> mintPolicyIds, boolean currentEpochOnly) {
        log.info("loading mint policy ids counts on epoch level...");

        val m = new HashMap<EpochKey<String>, Long>();

        val collection = "c6";

        val currentEpochNo = currentEpoch().orElseThrow();

        val epochs = currentEpochOnly ? Set.of(currentEpochNo) : Eras.epochsBetween(ALL.startEpoch(currentEpochNo), currentEpochNo);

        for (val epochNo : epochs) {
            mintPolicyIds.forEach(hash -> {
                val key = String.format("%s.%s.%d", collection, hash, epochNo);

                val c = redissonClient.getAtomicLong(key);
                if (c.isExists()) {
                    m.put(new EpochKey<>(epochNo, hash), c.get());
                } else {
                    m.put(new EpochKey<>(epochNo, hash), 0L);
                }
            });
        }

        return m;
    }

    public Map<String, Long> spendTransactionsCount(Collection<String> scriptHashes) {
        log.info("loading spend transactions count...");

        val m = new HashMap<String, Long>();

        val collection = "c3";

        scriptHashes.forEach(hash -> {
            val key = format("%s.%s", collection, hash);

            val c = redissonClient.getAtomicLong(key);

            if (c.isExists()) {
                m.put(hash, c.get());
            } else {
                m.put(hash, 0L);
            }
        });

        return m;
    }

    public Map<EpochKey<String>, Long> spendTransactionsCountEpoch(Collection<String> scriptHashes, boolean currentEpochOnly) {
        log.info("loading spend transactions count on epoch level...");

        val m = new HashMap<EpochKey<String>, Long>();

        val collection = "c4";

        val currentEpochNo = currentEpoch().orElseThrow();

        val epochs = currentEpochOnly ? Set.of(currentEpochNo) : Eras.epochsBetween(SnapshotType.ALL.startEpoch(currentEpochNo), currentEpochNo);

        for (val epochNo : epochs) {
            scriptHashes.forEach(hash -> {
                val epochKey = new EpochKey<>(epochNo, hash);

                val key = format("%s.%s.%s", collection, epochKey.getValue(), epochKey.getEpochNo());

                val c = redissonClient.getAtomicLong(key);

                if (c.isExists()) {
                    m.put(epochKey, c.get());
                } else {
                    m.put(epochKey, 0L);
                }
            });
        }

        return m;
    }

    public Map<String, Long> balance(Collection<String> hashes) {
        log.info("loading spend script balance...");

        val lockedPerHash = new HashMap<String, Long>();

        val collection = "c1";

        hashes.forEach(hash -> {
            val key = String.format("%s.%s", collection, hash);

            val r = redissonClient.getAtomicLong(key);

            if (r.isExists()) {
                val result = r.get();

                val resultADA = result / ONE_MLN;

                lockedPerHash.put(hash, resultADA);
            } else {
                lockedPerHash.put(hash, 0L);
            }
        });

        return lockedPerHash;
    }

    public Map<EpochKey<String>, Long> balanceWithEpoch(Collection<String> hashes,
                                                        boolean currentEpochOnly) {
        log.info("loading balance on epoch level...");

        val balanceMap = new HashMap<EpochKey<String>, Long>();

        val collection = "c2";

        val currentEpochNo = currentEpoch().orElseThrow();

        val epochs = currentEpochOnly ? Set.of(currentEpochNo) : Eras.epochsBetween(SnapshotType.ALL.startEpoch(currentEpochNo), currentEpochNo);

        for(val epochNo : epochs) {
            hashes.forEach(hash -> {
                val key = format("%s.%s.%d", collection, hash, epochNo);

                val r = redissonClient.getAtomicLong(key);

                if (r.isExists()) {
                    val result = r.get();

                    val resultAda = result / ONE_MLN;

                    balanceMap.put(new EpochKey<>(epochNo, hash), resultAda);
                } else {
                    balanceMap.put(new EpochKey<>(epochNo, hash), 0L);
                }
            });
        }

        return balanceMap;
    }

    public Map<String, Long> spendVolume(Collection<String> hashes) {
        log.info("loading spend volume data...");

        val volumePerHash = new HashMap<String, Long>();

        val collection = "c11";

        hashes.forEach(hash -> {
            val key = String.format("%s.%s", collection, hash);

            val r = redissonClient.getAtomicLong(key);

            if (r.isExists()) {
                val result = r.get();

                val resultADA = result / ONE_MLN;

                volumePerHash.put(hash, resultADA);
            } else {
                volumePerHash.put(hash, 0L);
            }
        });

        return volumePerHash;
    }

    public Map<EpochKey<String>, Long> spendVolumeEpochLevel(Collection<String> hashes,
                                                             boolean currentEpochOnly) {
        log.info("loading spend volume data on epoch level...");

        val volumeMap = new HashMap<EpochKey<String>, Long>();

        val collection = "c12";

        val currentEpochNo = currentEpoch().orElseThrow();

        val epochs = currentEpochOnly ? Set.of(currentEpochNo) : Eras.epochsBetween(SnapshotType.ALL.startEpoch(currentEpochNo), currentEpochNo);

        for (val epochNo : epochs) {
            hashes.forEach(hash -> {
                val key = format("%s.%s.%d", collection, hash, epochNo);

                val r = redissonClient.getAtomicLong(key);

                if (r.isExists()) {
                    val result = r.get();

                    val resultAda = result / ONE_MLN;

                    volumeMap.put(new EpochKey<>(epochNo, hash), resultAda);
                } else {
                    volumeMap.put(new EpochKey<>(epochNo, hash), 0L);
                }
            });
        }

        return volumeMap;
    }

    public Map<String, Long> spendFees(Collection<String> hashes) {
        log.info("loading spend fees data...");

        val feesMap = new HashMap<String, Long>();

        val collection = "c13";

        hashes.forEach(hash -> {
            val key = String.format("%s.%s", collection, hash);

            val r = redissonClient.getAtomicLong(key);

            if (r.isExists()) {
                val result = r.get();

                val resultADA = result / ONE_MLN;

                feesMap.put(hash, resultADA);
            } else {
                feesMap.put(hash, 0L);
            }
        });

        return feesMap;
    }

    public Map<EpochKey<String>, Long> spendFeesEpochLevel(Collection<String> hashes,
                                                           boolean currentEpochOnly) {
        log.info("loading spend fees on epoch level...");

        val feesPerEpoch = new HashMap<EpochKey<String>, Long>();

        val collection = "c14";

        val currentEpochNo = currentEpoch().orElseThrow();

        val epochs = currentEpochOnly ? Set.of(currentEpochNo) : Eras.epochsBetween(SnapshotType.ALL.startEpoch(currentEpochNo), currentEpochNo);

        for (val epochNo : epochs) {
            hashes.forEach(hash -> {
                val key = format("%s.%s.%d", collection, hash, epochNo);

                val r = redissonClient.getAtomicLong(key);

                if (r.isExists()) {
                    val result = r.get();

                    val resultAda = result / ONE_MLN;

                    feesPerEpoch.put(new EpochKey<>(epochNo, hash), resultAda);
                } else {
                    feesPerEpoch.put(new EpochKey<>(epochNo, hash), 0L);
                }
            });
        }

        return feesPerEpoch;
    }

    public Map<String, Long> spendTrxSizes(Collection<String> hashes) {
        log.info("loading trx sizes data...");

        val trxSizesPerHash = new HashMap<String, Long>();

        val collection = "c15";

        hashes.forEach(hash -> {
            log.debug("Loading trx sizes per hash:{}", hash);

            val key = String.format("%s.%s", collection, hash);

            val r = redissonClient.getAtomicLong(key);

            if (r.isExists()) {
                val result = r.get();

                trxSizesPerHash.put(hash, result);
            } else {
                trxSizesPerHash.put(hash, 0L);
            }
        });

        return trxSizesPerHash;
    }

    public Map<EpochKey<String>, Long> spendTrxSizesWithEpoch(Collection<String> hashes,
                                                              boolean currentEpochOnly) {
        log.info("loading trx sizes on epoch level...");

        val feesPerEpoch = new HashMap<EpochKey<String>, Long>();

        val collection = "c16";

        val currentEpochNo = currentEpoch().orElseThrow();

        val epochs = currentEpochOnly ? Set.of(currentEpochNo) : Eras.epochsBetween(SnapshotType.ALL.startEpoch(currentEpochNo), currentEpochNo);

        for (val epochNo : epochs) {
            hashes.forEach(hash -> {
                val key = format("%s.%s.%d", collection, hash, epochNo);

                val r = redissonClient.getAtomicLong(key);

                if (r.isExists()) {
                    val result = r.get();

                    feesPerEpoch.put(new EpochKey<>(epochNo, hash), result);
                } else {
                    feesPerEpoch.put(new EpochKey<>(epochNo, hash), 0L);
                }
            });
        }

        return feesPerEpoch;
    }

    public Map<String, Set<String>> spendUniqueAccounts(Collection<String> hashes) {
        log.info("loading spend uniqueAccounts...");

        val uniqueAccountsPerHash = new HashMap<String, Set<String>>();

        val collection = "c9";

        hashes.forEach(hash -> {
            log.debug("Loading unique accounts per hash:{}", hash);

            val key = format("%s.%s", collection, hash);

            val r = redissonClient.<String>getSet(key, new StringCodec());

            if (r.isExists()) {
                log.debug("Loading unique accounts for hash:{}, size:{}", hash, r.size());

                uniqueAccountsPerHash.put(hash, r);
            } else {
                uniqueAccountsPerHash.put(hash, Set.of());
            }
        });

        return uniqueAccountsPerHash;
    }

    public Map<String, Either<PoolError, String>> poolHexes(Collection<String> hashes) {
        log.info("loading pool hex, hashes size:{}", hashes.size());

        val poolHexByHash = new HashMap<String,  Either<PoolError, String>>();

        for (val hash : hashes) {
            if (hash.length() < 100) {
                poolHexByHash.put(hash, Either.left(PoolError.NOT_STAKED));
                continue;
            }

            val stakingPart = StringUtils.right( hash, 56);

            val collection = "c17";

            val key = format("%s.%s", collection, stakingPart);

            val poolRScored = redissonClient.<String>getScoredSortedSet(key, new StringCodec());

            val maybePool = poolRScored.valueRangeReversed(0, MAX_VALUE).stream().findFirst();

            if (maybePool.isPresent()) {
                poolHexByHash.put(hash, Either.right(maybePool.orElseThrow()));
            } else {
                poolHexByHash.put(hash, Either.left(PoolError.NOT_FOUND));
            }
        }

        return poolHexByHash;
    }

    public Map<EpochKey<String>, Set<String>> spendUniqueAccountsWithEpoch(Collection<String> hashes,
                                                                           boolean currentEpochOnly) {
        log.info("loading uniqueAccounts on epoch level...");

        val currentEpochNo = currentEpoch().orElseThrow();

        val epochs = currentEpochOnly ? Set.of(currentEpochNo) : Eras.epochsBetween(SnapshotType.ALL.startEpoch(currentEpochNo), currentEpochNo);

        return spendUniqueAccountsWithEpoch(hashes, epochs);
    }

    public Map<EpochKey<String>, Set<String>> spendUniqueAccountsWithEpoch(Collection<String> hashes,
                                                                           Set<Integer> epochs) {
        log.info("loading uniqueAccounts on epoch level for epochs");

        val uniqueAccountsPerAddress = new HashMap<EpochKey<String>, Set<String>>();

        val collection = "c10";

        for (val epochNo : epochs) {
            log.debug("Unique hashes - processing epochNo:{}", epochNo);

            hashes.forEach(hash -> {
                log.debug("Loading unique hashes, hash:{} for currentEpochNo:{}", hash, epochNo);

                val key = format("%s.%s.%d", collection, hash, epochNo);

                val r = redissonClient.<String>getSet(key, new StringCodec());

                if (r.isExists()) {
                    uniqueAccountsPerAddress.put(new EpochKey<>(epochNo, hash), r);
                } else {
                    uniqueAccountsPerAddress.put(new EpochKey<>(epochNo, hash), Set.of());
                }
            });
        }

        return uniqueAccountsPerAddress;
    }

    public Optional<Integer> currentEpoch() {
        val r = redissonClient.getAtomicLong("t0.epoch_no");

        if (r.isExists()) {
            return Optional.of((int) r.get());
        }

        return Optional.empty();
    }

    // current asset holders, that is per this epoch

    // ZRANGEBYSCORE "c14.026a18d04a0c642759bb3d83b12e3344894e5c1c7b2aeb1a2113a570.4c" 1 +inf
    public Set<String> getCurrentAssetHolders(String assetId) {
        log.info("gettiing current asset holders, assetId:{}", assetId);

        val collection = "c7";

        val assetMembersRScored = redissonClient.<String>getScoredSortedSet(format("%s.%s", collection, assetId), new StringCodec());
        val tokenHolders= assetMembersRScored.valueRangeReversed(1, MAX_VALUE);

        return Set.copyOf(tokenHolders);
    }

    // TODO allow to specify from which epoch / era to load data
    // ZRANGEBYSCORE "c14.026a18d04a0c642759bb3d83b12e3344894e5c1c7b2aeb1a2113a570.4c".364 1 +inf
    public Map<Integer, Set<String>> getAssetHoldersWithEpochs(String assetId, boolean currentEpochOnly) {
        log.info("gettiing current asset holders on epoch level, assetId:{}", assetId);

        val currentEpochNo = currentEpoch().orElseThrow();

        val epochs = currentEpochOnly ? Set.of(currentEpochNo) : Eras.epochsBetween(SnapshotType.ALL.startEpoch(currentEpochNo), currentEpochNo);

        val tokenHoldersPerEpoch = new HashMap<Integer, Set<String>>();

        for (val epoch : epochs) {
            val collection = "c8";

            val assetMembersRScored = redissonClient.<String>getScoredSortedSet(format("%s.%s.%d", collection, assetId, epoch), new StringCodec());

            val assetMembersPerEpoch = assetMembersRScored.stream().collect(Collectors.toSet());

            tokenHoldersPerEpoch.put(epoch, assetMembersPerEpoch);
        }

        return tokenHoldersPerEpoch;
    }

    public int getStoreGlobalUniqueAccountsCount() {
        val uniqueAccounts = redissonClient.getScoredSortedSet("e1", new StringCodec());

        if (uniqueAccounts.isExists()) {
            return uniqueAccounts.count(0, true, Double.MAX_VALUE, true);
        }

        log.error("e1 is not stored!!!");

        return 0;
    }

    public int storeGlobalUniqueAccounts(Set<String> hashes) {
        return sunion("e1", hashes);
    }

    public int storeDappEpochSnapshot(String dappId, Set<String> hashes, Set<Integer> epochs, SnapshotType snapshotType) {
        return sunionEpoch(String.format("e2.%s.%s", snapshotType.name(), dappId), hashes, epochs);
    }

    public int storeDappReleaseEpochSnapshot(String id, Set<String> hashes, Set<Integer> epochs, SnapshotType snapshotType) {
        return sunionEpoch(String.format("e3.%s.%s", snapshotType.name(), id), hashes, epochs);
    }

    public int getDappEpochEpochSnapshot(String dappId, SnapshotType snapshotType) {
        val uniqueAccounts = redissonClient.getScoredSortedSet(String.format("e2.%s.%s", snapshotType.name(), dappId), new StringCodec());

        if (uniqueAccounts.isExists()) {
            return uniqueAccounts.count(0, true, Double.MAX_VALUE, true);
        }

        log.warn("e2 is not stored for dapp id:{}", dappId);

        return 0;
    }

    public int getDappReleaseEpochSnapshot(String id, SnapshotType snapshotType) {
        val uniqueAccounts = redissonClient.getScoredSortedSet(String.format("e3.%s.%s", snapshotType.name(), id), new StringCodec());

        if (uniqueAccounts.isExists()) {
            return uniqueAccounts.count(0, true, Double.MAX_VALUE, true);
        }

        log.warn("e3 is not stored for dapp release id:{}", id);

        return 0;
    }

    private int sunion(String key, Set<String> hashes) {
        val collection = "c9";

        val uniqueAccounts = redissonClient.getScoredSortedSet(key, new StringCodec());

        val hkeys = new HashSet<>();


        for (val h : hashes) {
            val hkey = format("%s.%s", collection, h);
            hkeys.add(hkey);
        }

        if (hkeys.isEmpty()) {
            return 0;
        }

        return uniqueAccounts.union(hkeys.toArray(new String[] {}));
    }

    private int sunionEpoch(String key, Set<String> hashes, Set<Integer> epochs) {
        val collection = "c10";

        val uniqueAccounts = redissonClient.getScoredSortedSet(key, new StringCodec());

        val hkeys = new HashSet<>();

        for (val e : epochs) {
            for (val h : hashes) {
                val hkey = format("%s.%s.%d", collection, h, e);
                hkeys.add(hkey);
            }
        }

        if (hkeys.isEmpty()) {
            return 0;
        }

        return uniqueAccounts.union(hkeys.toArray(new String[] {}));
    }

    public Set<String> listSpendScriptHashes() {
        val result = new HashSet<String>();

        val collection = "c3";

        redissonClient.getKeys().getKeysByPattern(collection + ".*")
                .forEach(hash -> result.add(hash.replace(collection + ".", "")));

        return result;
    }

    public Set<String> listMintScriptHashes() {
        val result = new HashSet<String>();

        val collection = "c5";

        redissonClient.getKeys().getKeysByPattern(collection + ".*")
                .forEach(hash -> result.add(hash.replace(collection + ".", "")));

        return result;
    }

}


// 127.0.0.1:6379> sunion "c10.119068a7a3f008803edac87af1619860f2cdcde40c26987325ace138ad81728e7ed4cf324e1323135e7e6d931f01e30792d9cdf17129cb806d.373" "c10.119068a7a3f008803edac87af1619860f2cdcde40c26987325ace138ad81728e7ed4cf324e1323135e7e6d931f01e30792d9cdf17129cb806d.372"