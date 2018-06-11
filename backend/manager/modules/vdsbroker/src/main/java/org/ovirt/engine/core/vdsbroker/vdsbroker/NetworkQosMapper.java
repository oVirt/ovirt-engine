package org.ovirt.engine.core.vdsbroker.vdsbroker;

import java.util.HashMap;
import java.util.Map;

import org.ovirt.engine.core.common.businessentities.network.NetworkQoS;

public class NetworkQosMapper {

    // Megabit = 1000 *1000 bits, KiloBytes = 1000 bytes
    private static final int MEGABITS_TO_KILOBYTES = 125;
    private static final int MEGABYTES_TO_KILOBYTES = 1000;

    private final Map<String, Object> map;
    private final String inboundEntry;
    private final String outboundEntry;

    public NetworkQosMapper(Map<String, Object> map, String inboundEntry, String outboundEntry) {
        this.map = map;
        this.inboundEntry = inboundEntry;
        this.outboundEntry = outboundEntry;
    }

    public void serialize(NetworkQoS qos) {
        map.put(inboundEntry,
                constructQosData(qos.getInboundAverage(), qos.getInboundPeak(), qos.getInboundBurst()));
        map.put(outboundEntry,
                constructQosData(qos.getOutboundAverage(), qos.getOutboundPeak(), qos.getOutboundBurst()));
    }

    public NetworkQoS deserialize() {
        Map<String, Integer> inboundData = (Map<String, Integer>) map.get(inboundEntry);
        Map<String, Integer> outboundData = (Map<String, Integer>) map.get(outboundEntry);
        if (inboundData == null && outboundData == null) {
            return null;
        }

        NetworkQoS qos = new NetworkQoS();
        if (inboundData != null) {
            qos.setInboundAverage(divide(inboundData.get(VdsProperties.QOS_AVERAGE), MEGABITS_TO_KILOBYTES));
            qos.setInboundPeak(divide(inboundData.get(VdsProperties.QOS_PEAK), MEGABITS_TO_KILOBYTES));
            qos.setInboundBurst(divide(inboundData.get(VdsProperties.QOS_BURST), MEGABYTES_TO_KILOBYTES));
        }
        if (outboundData != null) {
            qos.setOutboundAverage(divide(outboundData.get(VdsProperties.QOS_AVERAGE), MEGABITS_TO_KILOBYTES));
            qos.setOutboundPeak(divide(outboundData.get(VdsProperties.QOS_PEAK), MEGABITS_TO_KILOBYTES));
            qos.setOutboundBurst(divide(outboundData.get(VdsProperties.QOS_BURST), MEGABYTES_TO_KILOBYTES));
        }
        return qos;
    }

    private Map<String, Integer> constructQosData(Integer average, Integer peak, Integer burst) {
        Map<String, Integer> qosData = new HashMap<>();
        if (average != null && average > 0) {
            qosData.put(VdsProperties.QOS_AVERAGE, average * MEGABITS_TO_KILOBYTES);
            qosData.put(VdsProperties.QOS_PEAK, peak * MEGABITS_TO_KILOBYTES);
            qosData.put(VdsProperties.QOS_BURST, burst * MEGABYTES_TO_KILOBYTES);
        }
        return qosData;
    }

    private static Integer divide(Integer num, int divisor) {
        if (num == null) {
            return null;
        } else {
            return (int) (num.floatValue() / divisor);
        }
    }

}
