package org.ovirt.engine.core.common.utils;

import java.util.HashMap;
import java.util.Map;

import org.ovirt.engine.core.common.businessentities.ConnectionMethod;
import org.ovirt.engine.core.common.businessentities.ExternalLocationInfo;
import org.ovirt.engine.core.common.businessentities.HttpLocationInfo;
import org.ovirt.engine.core.common.businessentities.LocationInfo;
import org.ovirt.engine.core.common.businessentities.ManagedBlockStorageLocationInfo;
import org.ovirt.engine.core.common.businessentities.VdsmImageLocationInfo;
import org.ovirt.engine.core.common.businessentities.storage.VolumeFormat;

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
            infoMap.put("prepared", info.isPrepared());
            if (info.getGeneration() != null) {
                infoMap.put("generation", info.getGeneration());
            }
            return infoMap;
        }

        if (locationInfo instanceof ManagedBlockStorageLocationInfo) {
            ManagedBlockStorageLocationInfo info = (ManagedBlockStorageLocationInfo) locationInfo;
            Map<String, Object> infoMap = new HashMap<>();
            infoMap.put("lease", info.getLease());
            infoMap.put("url", info.getUrl());
            infoMap.put("generation", info.getGeneration());
            infoMap.put("format", volumeFormatToString(info.getFormat()));
            infoMap.put("is_zero", info.isZeroed());
            infoMap.put("endpoint_type", "external");

            return infoMap;
        }

        throw new RuntimeException("Unsupported location info");
    }

    private static String volumeFormatToString(VolumeFormat format) {
        switch (format) {
        case COW:
            return "cow";
        case RAW:
            return "raw";
        default:
            throw new RuntimeException("Invalid format");
        }
    }
}
