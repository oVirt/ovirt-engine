package org.ovirt.engine.core.common.utils;

import java.util.HashMap;
import java.util.Map;

import org.ovirt.engine.core.common.businessentities.SubchainInfo;

public class SubchainInfoHelper {

    private SubchainInfoHelper() {
    }

    public static Map<String, String> prepareSubchainInfoForVdsCommand(SubchainInfo subchainInfo) {
        Map<String, String> map = new HashMap<>();
        map.put("sd_id", subchainInfo.getStorageDomainId().toString());
        map.put("img_id", subchainInfo.getImageGroupId().toString());
        map.put("base_id", subchainInfo.getBaseImageId().toString());
        map.put("top_id", subchainInfo.getTopImageId().toString());
        return map;
    }
}
