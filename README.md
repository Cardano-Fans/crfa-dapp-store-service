# crfa-dapp-store-service

## Requirements
- JDK17

## TODO
Shorter term:
- crfa-dapp-store: snapshots support for timeline component / think about how we can do it?



- crfa-dapp-store: all stats per category + all stats per sub category + those on epoch level
- crfa-dapp-store: pool ticker for a script address using blockfrost on dapp release / dapp level
- scrolls: mint fees, mint avg size, mint avg fee
- scrolls: adjusted volume
- support unique accounts on all levels including global level + epoch, needs db changes I guess?

Incremental ingestion:
- move to only use epoch level tables (realtime)
- create redis listener to update data as it comes (realtime)

Optimisations:
- consider using bloom filters instead of HashSet (memory optimisation only)
- use hibernate H2 with file storage instead of clunky ORM (https://www.baeldung.com/h2-embedded-db-data-storage)

Longer term:
- top 25 / 100 resource hogs, which dapps use the most expensive and most size intensive scripts?
- top 25 / 100 script addresses / dapps in terms of dapp volume?
- top 25 / 100 script addresses / dapps in terms of dapp avg transaction fee?

- scrolls: user contract transactions vs all contract transactions (wallet CONNECTOR). Feature to discuss, maybe users don't need it???
