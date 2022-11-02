# crfa-dapp-store-service

## Requirements
- JDK17

## TODO
Shorter term:
- crfa-dapp-store: all stats per category + all stats per sub category + those on epoch level
- crfa-dapp-store: pool ticker for a script address using blockfrost
- crfa-dapp-store: total trx fees, avg trx fees, avg trx size

Incremental ingestion:
- move to only use epoch level tables
- create redis listener to update data as it comes

Optimisations:
- consider using bloom filters instead of HashSet (memory optimisation only)
- use hibernate H2 with file storage instead of clunky ORM (https://www.baeldung.com/h2-embedded-db-data-storage)

Longer term:
- scrolls: adjusted volume so we don't include returning transaction
- top 25 / 100 resource hogs, which dapps use the most expensive and most size intensive scripts?
- top 25 / 100 script addresses / dapps in terms of dapp volume?
- top 25 / 100 script addresses / dapps in terms of dapp avg transaction fee?

- scrolls: user contract transactions vs all contract transactions (wallet CONNECTOR). Feature to discuss, maybe users don't need it???