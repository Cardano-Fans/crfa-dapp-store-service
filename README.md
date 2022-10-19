# crfa-dapp-store-service

## Requirements
- JDK17

## TODO
- scrolls: total fees genereted by script address, dapp release and of course dapp root level but also over epoch

- scrolls: set(tx size) by script addr, benefit is that we can to e.g. median fee or min fee, max fee per script address, etc
- scrolls: set(tx fee) by script addr
===
- scrolls: volume fixes 
- 
-
- scrolls: avg transaction size per script addr, same over epochs across all dapps
- scrolls: avg transaction fee per script addr, same over epochs across all dapps
- top 25 resource hogs, which dapps use the most expensive and most size intensive scripts
- top 25 addresses in terms of dapp volume

- consider using bloom filters instead of HashSet (memory optimisation only)

- scrolls: user contract transactions vs all contract transactions (wallet CONNECTOR)

REALLY EASY since we already have a reducer for it:
- dashboard general epoch level stats: volume, unique accounts, inflowsOutflows, trxCount
- 