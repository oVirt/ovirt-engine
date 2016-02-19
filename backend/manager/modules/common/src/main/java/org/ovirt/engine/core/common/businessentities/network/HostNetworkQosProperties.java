package org.ovirt.engine.core.common.businessentities.network;

import java.io.Serializable;
import java.util.Objects;

import org.ovirt.engine.core.common.config.ConfigValues;
import org.ovirt.engine.core.common.validation.annotation.ConfiguredRange;

public class HostNetworkQosProperties implements Serializable {

    private static final long serialVersionUID = -4137292757583671432L;

    public HostNetworkQosProperties() {
    }

    public HostNetworkQosProperties(HostNetworkQosProperties original) {
        this.outAverageLinkshare = original.outAverageLinkshare;
        this.outAverageUpperlimit = original.outAverageUpperlimit;
        this.outAverageRealtime = original.outAverageRealtime;
    }

    /**
     * Unit: Mbps
     */
    @ConfiguredRange(min = 1, maxConfigValue = ConfigValues.MaxHostNetworkQosShares,
            message = "ACTION_TYPE_FAILED_NETWORK_QOS_OUT_OF_RANGE_VALUES")
    private Integer outAverageLinkshare;

    /**
     * Unit: Mbps
     */
    @ConfiguredRange(min = 1, maxConfigValue = ConfigValues.MaxAverageNetworkQoSValue,
            message = "ACTION_TYPE_FAILED_NETWORK_QOS_OUT_OF_RANGE_VALUES")
    private Integer outAverageUpperlimit;

    /**
     * Unit: Mbps
     */
    @ConfiguredRange(min = 1, maxConfigValue = ConfigValues.MaxAverageNetworkQoSValue,
            message = "ACTION_TYPE_FAILED_NETWORK_QOS_OUT_OF_RANGE_VALUES")
    private Integer outAverageRealtime;

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
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof HostNetworkQosProperties)) {
            return false;
        }
        HostNetworkQosProperties that = (HostNetworkQosProperties) o;
        return Objects.equals(getOutAverageLinkshare(), that.getOutAverageLinkshare()) &&
                Objects.equals(getOutAverageUpperlimit(), that.getOutAverageUpperlimit()) &&
                Objects.equals(getOutAverageRealtime(), that.getOutAverageRealtime());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getOutAverageLinkshare(), getOutAverageUpperlimit(), getOutAverageRealtime());
    }
}
