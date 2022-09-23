package crfa.app.domain;

import lombok.Builder;

import java.util.List;
import java.util.Map;
import java.util.Set;

@Builder
public class DataPointers {

    public Set<AddressPointers> addressPointersList;

    public List<String> mintPolicyIds;

    // key = asset_id / hex, value: set of holders
    public Map<String, Set<String>> assetNameHexesToTokenHolders;

    public Map<EpochValue<String>, Set<String>> assetNameHexesToTokenHoldersWithEpoch;

    public List<String> contractAddresses;

    public List<String> scriptHashes;

}
