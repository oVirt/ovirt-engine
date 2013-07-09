package org.ovirt.engine.core.common.businessentities.network;


import org.ovirt.engine.core.common.businessentities.BusinessEntitiesDefinitions;
import org.ovirt.engine.core.common.businessentities.BusinessEntity;
import org.ovirt.engine.core.common.businessentities.IVdcQueryable;
import org.ovirt.engine.core.common.config.ConfigValues;
import org.ovirt.engine.core.common.utils.ObjectUtils;
import org.ovirt.engine.core.common.validation.annotation.ConfiguredRange;
import org.ovirt.engine.core.common.validation.annotation.ValidI18NName;
import org.ovirt.engine.core.compat.Guid;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.io.Serializable;

public class NetworkQoS extends IVdcQueryable implements Serializable, BusinessEntity<Guid> {

    private static final long serialVersionUID = 1122772549710787758L;

    @NotNull(message = "QOS_NAME_NOT_NULL")
    @Size(min = 1, max = BusinessEntitiesDefinitions.GENERAL_NAME_SIZE, message = "QOS_NAME_TOO_LONG")
    @ValidI18NName(message = "QOS_NAME_INVALID")
    private String name;

    @NotNull(message = "ACTION_TYPE_FAILED_NETWORK_QOS_INVALID_DC_ID")
    private Guid storagePoolId;

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

    private Guid id;


    @Override
    public Guid getId() {
        return id;
    }

    @Override
    public void setId(Guid id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Guid getStoragePoolId() {
        return storagePoolId;
    }

    public void setStoragePoolId(Guid storagePoolId) {
        this.storagePoolId = storagePoolId;
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
        return ObjectUtils.objectsEqual(this.getInboundAverage(), other.getInboundAverage())
                && ObjectUtils.objectsEqual(this.getInboundPeak(), other.getInboundPeak())
                && ObjectUtils.objectsEqual(this.getInboundBurst(), other.getInboundBurst())
                && ObjectUtils.objectsEqual(this.getOutboundAverage(), other.getOutboundAverage())
                && ObjectUtils.objectsEqual(this.getOutboundPeak(), other.getOutboundPeak())
                && ObjectUtils.objectsEqual(this.getOutboundBurst(), other.getOutboundBurst());
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null) {
            return false;
        }
        if (!(o instanceof NetworkQoS)) {
            return false;
        }
        NetworkQoS other = (NetworkQoS) o;
        return ObjectUtils.objectsEqual(this.getName(), other.getName())
                && ObjectUtils.objectsEqual(this.getStoragePoolId(), other.getStoragePoolId())
                && equalValues(other);
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((id == null) ? 0 : id.hashCode());
        result = prime * result + ((name == null) ? 0 : name.hashCode());
        result = prime * result + ((storagePoolId == null) ? 0 : storagePoolId.hashCode());
        result = prime * result + ((inboundAverage == null) ? 0 : inboundAverage.hashCode());
        result = prime * result + ((inboundPeak == null) ? 0 : inboundPeak.hashCode());
        result = prime * result + ((inboundBurst == null) ? 0 : inboundBurst.hashCode());
        result = prime * result + ((outboundAverage == null) ? 0 : outboundAverage.hashCode());
        result = prime * result + ((outboundPeak == null) ? 0 : outboundPeak.hashCode());
        result = prime * result + ((outboundBurst == null) ? 0 : outboundBurst.hashCode());
        return result;
    }

    @Override
    public Object getQueryableId() {
        return getId();
    }
}
