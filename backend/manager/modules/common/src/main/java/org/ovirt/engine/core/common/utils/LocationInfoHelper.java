package org.ovirt.engine.core.common.utils;

import java.util.HashMap;
import java.util.Map;

import org.ovirt.engine.core.common.businessentities.ConnectionMethod;
import org.ovirt.engine.core.common.businessentities.ExternalLocationInfo;
import org.ovirt.engine.core.common.businessentities.HttpLocationInfo;
import org.ovirt.engine.core.common.businessentities.LocationInfo;
import org.ovirt.engine.core.common.businessentities.VdsmImageLocationInfo;

public class LocationInfoHelper {
    private LocationInfoHelper() {
    }

    public static Map<String, Object> prepareLocationInfoForVdsCommand(LocationInfo locationInfo) {
        if (locationInfo instanceof ExternalLocationInfo) {
            ExternalLocationInfo info = (ExternalLocationInfo)locationInfo;
            if (ConnectionMethod.HTTP.equals(info.getConnectionMethod())) {
                HttpLocationInfo httpInfo = (HttpLocationInfo) info;
                Map<String, Object> infoMap = new HashMap<>();
                infoMap.put("method", "http");
                infoMap.put("url", httpInfo.getUrl());
                infoMap.put("headers", httpInfo.getHeaders());
                return infoMap;
            }
        }

        if (locationInfo instanceof VdsmImageLocationInfo) {
            VdsmImageLocationInfo info = (VdsmImageLocationInfo)locationInfo;
            Map<String, Object> infoMap = new HashMap<>();
            infoMap.put("endpoint_type", "div");
            infoMap.put("sd_id", info.getStorageDomainId().toString());
            infoMap.put("img_id", info.getImageGroupId().toString());
            infoMap.put("vol_id", info.getImageId().toString());
            return infoMap;
        }

        throw new RuntimeException("Unsupported location info");
    }
}
