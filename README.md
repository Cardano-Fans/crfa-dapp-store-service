# crfa-dapp-store-service

## Requirements
- JDK17

## TODO
Shorter term:
- crfa-dapp-store: all stats per category + all stats per sub category
- crfa-dapp-store: pool ticker for a script address using blockfrost

- crfa-dapp-store: set(tx size) by script addr - total size shows by how many bytes ledger grows
- crfa-dapp-store: set(avg tx size) by script addr, this allows us to see is script / dapp is a storage guzzler, e.g. does not use Plutus V2 or uses unoptimised code

Optimisations:
- consider using bloom filters instead of HashSet (memory optimisation only)
- use hibernate H2 with file storage instead of clunky ORM (https://www.baeldung.com/h2-embedded-db-data-storage)

Longer term:
- scrolls: adjusted volume so we don't include returning transaction
- top 25 / 100 resource hogs, which dapps use the most expensive and most size intensive scripts?
- top 25 / 100 script addresses / dapps in terms of dapp volume?
- top 25 / 100 script addresses / dapps in terms of dapp avg transaction fee?

- scrolls: user contract transactions vs all contract transactions (wallet CONNECTOR). Feature to discuss, maybe users don't need it???