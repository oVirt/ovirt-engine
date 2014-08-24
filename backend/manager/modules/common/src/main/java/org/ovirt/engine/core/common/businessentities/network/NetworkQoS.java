package org.ovirt.engine.core.common.businessentities.network;


import org.ovirt.engine.core.common.businessentities.qos.QosBase;
import org.ovirt.engine.core.common.businessentities.qos.QosType;
import org.ovirt.engine.core.common.config.ConfigValues;
import org.ovirt.engine.core.common.utils.ObjectUtils;
import org.ovirt.engine.core.common.validation.annotation.ConfiguredRange;

public class NetworkQoS extends QosBase {

    private static final long serialVersionUID = 1122772549710787758L;

    @ConfiguredRange(min = 0, maxConfigValue = ConfigValues.MaxAverageNetworkQoSValue,
            message = "ACTION_TYPE_FAILED_NETWORK_QOS_OUT_OF_RANGE_VALUES")
    private Integer inboundAverage;

    @ConfiguredRange(min = 0, maxConfigValue = ConfigValues.MaxPeakNetworkQoSValue,
            message = "ACTION_TYPE_FAILED_NETWORK_QOS_OUT_OF_RANGE_VALUES")
    private Integer inboundPeak;

    @ConfiguredRange(min = 0, maxConfigValue = ConfigValues.MaxBurstNetworkQoSValue,
            message = "ACTION_TYPE_FAILED_NETWORK_QOS_OUT_OF_RANGE_VALUES")
    private Integer inboundBurst;

    @ConfiguredRange(min = 0, maxConfigValue = ConfigValues.MaxAverageNetworkQoSValue,
            message = "ACTION_TYPE_FAILED_NETWORK_QOS_OUT_OF_RANGE_VALUES")
    private Integer outboundAverage;

    @ConfiguredRange(min = 0, maxConfigValue = ConfigValues.MaxPeakNetworkQoSValue,
            message = "ACTION_TYPE_FAILED_NETWORK_QOS_OUT_OF_RANGE_VALUES")
    private Integer outboundPeak;

    @ConfiguredRange(min = 0, maxConfigValue = ConfigValues.MaxBurstNetworkQoSValue,
            message = "ACTION_TYPE_FAILED_NETWORK_QOS_OUT_OF_RANGE_VALUES")
    private Integer outboundBurst;

    public NetworkQoS() {
        super(QosType.NETWORK);
    }

    public Integer getInboundAverage() {
        return inboundAverage;
    }

    public void setInboundAverage(Integer inboundAverage) {
        this.inboundAverage = inboundAverage;
    }

    public Integer getInboundPeak() {
        return inboundPeak;
    }

    public void setInboundPeak(Integer inboundPeak) {
        this.inboundPeak = inboundPeak;
    }

    public Integer getInboundBurst() {
        return inboundBurst;
    }

    public void setInboundBurst(Integer inboundBurst) {
        this.inboundBurst = inboundBurst;
    }

    public Integer getOutboundAverage() {
        return outboundAverage;
    }

    public void setOutboundAverage(Integer outboundAverage) {
        this.outboundAverage = outboundAverage;
    }

    public Integer getOutboundPeak() {
        return outboundPeak;
    }

    public void setOutboundPeak(Integer outboundPeak) {
        this.outboundPeak = outboundPeak;
    }

    public Integer getOutboundBurst() {
        return outboundBurst;
    }

    public void setOutboundBurst(Integer outboundBurst) {
        this.outboundBurst = outboundBurst;
    }

    public boolean equalValues(NetworkQoS other) {
        return ObjectUtils.objectsEqual(getInboundAverage(), other.getInboundAverage())
                && ObjectUtils.objectsEqual(getInboundPeak(), other.getInboundPeak())
                && ObjectUtils.objectsEqual(getInboundBurst(), other.getInboundBurst())
                && ObjectUtils.objectsEqual(getOutboundAverage(), other.getOutboundAverage())
                && ObjectUtils.objectsEqual(getOutboundPeak(), other.getOutboundPeak())
                && ObjectUtils.objectsEqual(getOutboundBurst(), other.getOutboundBurst());
    }

    @Override
    public boolean equals(Object o) {
        return super.equals(o) &&
                equalValues((NetworkQoS) o);
    }

    @Override
    public String getString() {
        StringBuilder builder = new StringBuilder();
        builder.append("[")
               .append("inbound ")
               .append("{")
               .append("average=")
               .append(getInboundAverage())
               .append(", peak=")
               .append(getInboundPeak())
               .append(", burst=")
               .append(getInboundBurst())
               .append("}, ")
               .append("outbound ")
               .append("{")
               .append("average=")
               .append(getOutboundAverage())
               .append(", peak=")
               .append(getOutboundPeak())
               .append(", burst=")
               .append(getOutboundBurst())
               .append("}")
               .append("]");
        return builder.toString();
    }

    @Override
    public String toString() {
        return getString();
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = super.hashCode();
        result = prime * result + ((inboundAverage == null) ? 0 : inboundAverage.hashCode());
        result = prime * result + ((inboundPeak == null) ? 0 : inboundPeak.hashCode());
        result = prime * result + ((inboundBurst == null) ? 0 : inboundBurst.hashCode());
        result = prime * result + ((outboundAverage == null) ? 0 : outboundAverage.hashCode());
        result = prime * result + ((outboundPeak == null) ? 0 : outboundPeak.hashCode());
        result = prime * result + ((outboundBurst == null) ? 0 : outboundBurst.hashCode());
        return result;
    }

}
