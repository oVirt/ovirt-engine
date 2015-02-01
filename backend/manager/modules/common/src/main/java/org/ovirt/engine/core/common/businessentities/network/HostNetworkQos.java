package org.ovirt.engine.core.common.businessentities.network;

import org.ovirt.engine.core.common.businessentities.qos.QosBase;
import org.ovirt.engine.core.common.businessentities.qos.QosType;
import org.ovirt.engine.core.common.config.ConfigValues;
import org.ovirt.engine.core.common.utils.ObjectUtils;
import org.ovirt.engine.core.common.utils.ToStringBuilder;
import org.ovirt.engine.core.common.validation.annotation.ConfiguredRange;

public class HostNetworkQos extends QosBase {

    private static final long serialVersionUID = -5062624700835301848L;

    @ConfiguredRange(min = 1, maxConfigValue = ConfigValues.MaxHostNetworkQosShares,
            message = "ACTION_TYPE_FAILED_NETWORK_QOS_OUT_OF_RANGE_VALUES")
    private Integer outAverageLinkshare;

    @ConfiguredRange(min = 0, maxConfigValue = ConfigValues.MaxAverageNetworkQoSValue,
            message = "ACTION_TYPE_FAILED_NETWORK_QOS_OUT_OF_RANGE_VALUES")
    private Integer outAverageUpperlimit;

    @ConfiguredRange(min = 0, maxConfigValue = ConfigValues.MaxAverageNetworkQoSValue,
            message = "ACTION_TYPE_FAILED_NETWORK_QOS_OUT_OF_RANGE_VALUES")
    private Integer outAverageRealtime;

    public HostNetworkQos() {
        super(QosType.HOSTNETWORK);
    }

    public Integer getOutAverageLinkshare() {
        return outAverageLinkshare;
    }

    public void setOutAverageLinkshare(Integer outAverageLinkshare) {
        this.outAverageLinkshare = outAverageLinkshare;
    }

    public Integer getOutAverageUpperlimit() {
        return outAverageUpperlimit;
    }

    public void setOutAverageUpperlimit(Integer outAverageUpperlimit) {
        this.outAverageUpperlimit = outAverageUpperlimit;
    }

    public Integer getOutAverageRealtime() {
        return outAverageRealtime;
    }

    public void setOutAverageRealtime(Integer outAverageRealtime) {
        this.outAverageRealtime = outAverageRealtime;
    }

    public boolean isEmpty() {
        return getOutAverageLinkshare() == null && getOutAverageUpperlimit() == null && getOutAverageRealtime() == null;
    }

    @Override
    public String toString() {
        return ToStringBuilder.forInstance(this)
                .append("outAverageLinkshare", renderQosParameter(outAverageLinkshare))
                .append("outAverageUpperlimit", renderQosParameter(outAverageUpperlimit))
                .append("outAverageRealtime", renderQosParameter(outAverageRealtime))
                .build();
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = super.hashCode();
        result = prime * result + ((outAverageLinkshare == null) ? 0 : outAverageLinkshare.hashCode());
        result = prime * result + ((outAverageRealtime == null) ? 0 : outAverageRealtime.hashCode());
        result = prime * result + ((outAverageUpperlimit == null) ? 0 : outAverageUpperlimit.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (!super.equals(obj))
            return false;

        HostNetworkQos other = (HostNetworkQos) obj;
        return ObjectUtils.objectsEqual(getOutAverageLinkshare(), other.getOutAverageLinkshare())
                && ObjectUtils.objectsEqual(getOutAverageUpperlimit(), other.getOutAverageUpperlimit())
                && ObjectUtils.objectsEqual(getOutAverageRealtime(), other.getOutAverageRealtime());
    }

}
