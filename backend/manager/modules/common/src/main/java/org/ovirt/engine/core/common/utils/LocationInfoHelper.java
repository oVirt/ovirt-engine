package org.ovirt.engine.core.common.utils;

import java.util.HashMap;
import java.util.Map;

import org.ovirt.engine.core.common.businessentities.ConnectionMethod;
import org.ovirt.engine.core.common.businessentities.HttpLocationInfo;
import org.ovirt.engine.core.common.businessentities.LocationInfo;

public class LocationInfoHelper {
    private LocationInfoHelper() {
    }

    public static Map<String, Object> prepareLocationInfoForVdsCommand(LocationInfo info) {
        if (ConnectionMethod.HTTP.equals(info.getConnectionMethod())) {
            HttpLocationInfo httpInfo = (HttpLocationInfo)info;
            Map<String, Object> infoMap = new HashMap<String, Object>();
            infoMap.put("method", "http");
            infoMap.put("url", httpInfo.getUrl());
            infoMap.put("headers", httpInfo.getHeaders());
            return infoMap;
        } else {
            throw new RuntimeException("Unsupported connection method");
        }
    }
}
