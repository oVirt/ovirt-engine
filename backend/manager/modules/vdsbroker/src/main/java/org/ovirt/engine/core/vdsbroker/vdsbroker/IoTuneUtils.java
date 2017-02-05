package org.ovirt.engine.core.vdsbroker.vdsbroker;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.ovirt.engine.core.common.businessentities.qos.StorageQos;
import org.ovirt.engine.core.common.utils.Pair;

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
        Map<String, Long> ioTuneMap = new HashMap<>();

        // Convert MiB/s to B/s vdsm is expecting
        ioTuneMap.put(VdsProperties.TotalBytesSec, convertThroughput(storageQos.getMaxThroughput()));
        ioTuneMap.put(VdsProperties.ReadBytesSec,  convertThroughput(storageQos.getMaxReadThroughput()));
        ioTuneMap.put(VdsProperties.WriteBytesSec, convertThroughput(storageQos.getMaxWriteThroughput()));

        ioTuneMap.put(VdsProperties.TotalIopsSec, convertIops(storageQos.getMaxIops()));
        ioTuneMap.put(VdsProperties.ReadIopsSec,  convertIops(storageQos.getMaxReadIops()));
        ioTuneMap.put(VdsProperties.WriteIopsSec, convertIops(storageQos.getMaxWriteIops()));

        return ioTuneMap;
    }

    /**
     * Note that the returned list is supposed to be sorted by the property name (key)
     */
    public static List<Pair<String, Long>> ioTuneListFrom(StorageQos storageQos) {
        List<Pair<String, Long>> ioTuneList = new ArrayList<>();
        ioTuneList.add(new Pair<>(VdsProperties.ReadBytesSec,  convertThroughput(storageQos.getMaxReadThroughput())));
        ioTuneList.add(new Pair<>(VdsProperties.ReadIopsSec,  convertIops(storageQos.getMaxReadIops())));
        ioTuneList.add(new Pair<>(VdsProperties.TotalBytesSec, convertThroughput(storageQos.getMaxThroughput())));
        ioTuneList.add(new Pair<>(VdsProperties.TotalIopsSec, convertIops(storageQos.getMaxIops())));
        ioTuneList.add(new Pair<>(VdsProperties.WriteBytesSec, convertThroughput(storageQos.getMaxWriteThroughput())));
        ioTuneList.add(new Pair<>(VdsProperties.WriteIopsSec, convertIops(storageQos.getMaxWriteIops())));
        return ioTuneList;
    }
}
