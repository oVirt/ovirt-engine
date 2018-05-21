package org.ovirt.engine.core.vdsbroker.vdsbroker;

import java.util.LinkedHashMap;
import java.util.Map;

import org.ovirt.engine.core.common.businessentities.qos.StorageQos;

public class IoTuneUtils {

    public static final long MB_TO_BYTES = 1024L * 1024L;
    private static long convertThroughput(Integer value) {
        // Libvirt interprets 0 as unlimited
        return (value != null) ? value.longValue() * MB_TO_BYTES : 0L;
    }

    private static long convertIops(Integer value) {
        return (value != null) ? value.longValue() : 0L;
    }

    public static Map<String, Long> ioTuneMapFrom(StorageQos storageQos) {
        Map<String, Long> ioTuneMap = new LinkedHashMap<>();

        ioTuneMap.put(VdsProperties.TotalBytesSec, convertThroughput(storageQos.getMaxThroughput()));
        ioTuneMap.put(VdsProperties.TotalIopsSec, convertIops(storageQos.getMaxIops()));
        ioTuneMap.put(VdsProperties.ReadBytesSec,  convertThroughput(storageQos.getMaxReadThroughput()));
        ioTuneMap.put(VdsProperties.ReadIopsSec,  convertIops(storageQos.getMaxReadIops()));
        ioTuneMap.put(VdsProperties.WriteBytesSec, convertThroughput(storageQos.getMaxWriteThroughput()));
        ioTuneMap.put(VdsProperties.WriteIopsSec, convertIops(storageQos.getMaxWriteIops()));

        return ioTuneMap;
    }
}
