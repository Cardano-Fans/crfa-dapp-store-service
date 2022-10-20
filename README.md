# crfa-dapp-store-service

## Requirements
- JDK17

## TODO
- crfa-dapp-store: total fees genereted by script address, dapp release and of course dapp root level but also over epoch

- crfa-dapp-store: set(tx size) by script addr, benefit is that we can to e.g. median fee or min fee, max fee per script address, etc
- crfa-dapp-store: set(tx fee) by script addr
- crfa-dapp-store:: avg transaction size per script addr, same over epochs across all dapps
- crfa-dapp-store:: avg transaction fee per script addr, same over epochs across all dapps
- crfa-dapp-store: pool ticker for a script address using blockfrost
- crfa-dapp-store: all stats per category + all stats per sub category

- ===
- scrolls: volume fixes 
- top 25 resource hogs, which dapps use the most expensive and most size intensive scripts
- top 25 addresses in terms of dapp volume
  
- consider using bloom filters instead of HashSet (memory optimisation only)

- scrolls: user contract transactions vs all contract transactions (wallet CONNECTOR)

REALLY EASY since we already have a reducer for it:
- dashboard general epoch level stats: volume, unique accounts, inflowsOutflows, trxCount
- 