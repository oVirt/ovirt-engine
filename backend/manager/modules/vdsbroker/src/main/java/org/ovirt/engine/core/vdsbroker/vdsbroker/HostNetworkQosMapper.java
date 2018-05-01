package org.ovirt.engine.core.vdsbroker.vdsbroker;

import java.util.HashMap;
import java.util.Map;

import org.ovirt.engine.core.common.businessentities.network.HostNetworkQos;

public class HostNetworkQosMapper {

    // Mbits (mega bits) = 1000 * 1000 bits
    private static final long MBITS_TO_BITS = 1000 * 1000L;

    private final Map<String, Object> rootEntry;

    public HostNetworkQosMapper(Map<String, Object> rootEntry) {
        this.rootEntry = rootEntry;
    }

    public void serialize(HostNetworkQos qos) {
        if (qos == null) {
            return;
        }

        Map<String, Object> outboundEntry = new HashMap<>();
        serializeValue(outboundEntry, VdsProperties.HOST_QOS_LINKSHARE, qos.getOutAverageLinkshare());
        serializeValue(outboundEntry, VdsProperties.HOST_QOS_UPPERLIMIT, qos.getOutAverageUpperlimit(), MBITS_TO_BITS);
        serializeValue(outboundEntry, VdsProperties.HOST_QOS_REALTIME, qos.getOutAverageRealtime(), MBITS_TO_BITS);
        if (outboundEntry.isEmpty()) {
            return;
        }

        Map<String, Object> qosEntry = new HashMap<>();
        qosEntry.put(VdsProperties.HOST_QOS_OUTBOUND, outboundEntry);

        rootEntry.put(VdsProperties.HOST_QOS, qosEntry);
    }

    private void serializeValue(Map<String, Object> entry, String curveKey, Integer value) {
        serializeValue(entry, curveKey, value, 1L);
    }

    private void serializeValue(Map<String, Object> entry, String curveKey, Integer value, long conversionRate) {
        if (value != null) {
            Map<String, Long> parameters = new HashMap<>();
            parameters.put(VdsProperties.HOST_QOS_AVERAGE, value * conversionRate);
            entry.put(curveKey, parameters);
        }
    }

    public HostNetworkQos deserialize() {
        Map<String, Object> qosEntry = (Map<String, Object>) rootEntry.get(VdsProperties.HOST_QOS);
        if (qosEntry == null) {
            return null;
        }

        Map<String, Object> outboundEntry = (Map<String, Object>) qosEntry.get(VdsProperties.HOST_QOS_OUTBOUND);
        if (outboundEntry == null) {
            return null;
        }

        // name and DC ID are not set on purpose - anonymous QoS shouldn't have any!
        HostNetworkQos qos = new HostNetworkQos();
        qos.setOutAverageLinkshare(deserializeValue(outboundEntry, VdsProperties.HOST_QOS_LINKSHARE));
        qos.setOutAverageUpperlimit(deserializeValue(outboundEntry, VdsProperties.HOST_QOS_UPPERLIMIT, MBITS_TO_BITS));
        qos.setOutAverageRealtime(deserializeValue(outboundEntry, VdsProperties.HOST_QOS_REALTIME, MBITS_TO_BITS));
        return qos.isEmpty() ? null : qos;
    }

    private Integer deserializeValue(Map<String, Object> entry, String curveKey) {
        return deserializeValue(entry, curveKey, 1L);
    }

    private Integer deserializeValue(Map<String, Object> entry, String curveKey, long conversionRate) {
        Map<String, Object> parameters = (Map<String, Object>) entry.get(curveKey);
        if (parameters == null || parameters.get(VdsProperties.HOST_QOS_AVERAGE) == null) {
            return null;
        }
        // json-rpc de-serializes a value to integer or to long according
        // to its magnitude, so convert int to long
        long avg = ((Number) parameters.get(VdsProperties.HOST_QOS_AVERAGE)).longValue();
        return (int)(avg / conversionRate);
    }
}
