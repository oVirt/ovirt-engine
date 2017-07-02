package org.ovirt.engine.core.common.businessentities.gluster;

import java.util.Objects;

import org.ovirt.engine.core.common.businessentities.Queryable;
import org.ovirt.engine.core.common.utils.ToStringBuilder;
import org.ovirt.engine.core.compat.Guid;

public class GlusterVolumeSnapshotConfig implements Queryable {
    private static final long serialVersionUID = 3432543544365L;
    private Guid clusterId;
    private Guid volumeId;
    private String paramName;
    private String paramValue;

    public GlusterVolumeSnapshotConfig() {
    }

    public GlusterVolumeSnapshotConfig(Guid clusterId, Guid volumeId, String paramName, String paramValue) {
        this.clusterId = clusterId;
        this.volumeId = volumeId;
        this.paramName = paramName;
        this.paramValue = paramValue;
    }

    public Guid getClusterId() {
        return this.clusterId;
    }

    public void setClusterId(Guid cid) {
        this.clusterId = cid;
    }

    public Guid getVolumeId() {
        return volumeId;
    }

    @Override
    public Object getQueryableId() {
        return getVolumeId();
    }

    public void setVolumeId(Guid volumeId) {
        this.volumeId = volumeId;
    }

    public String getParamName() {
        return paramName;
    }

    public void setParamName(String paramName) {
        this.paramName = paramName;
    }

    public String getParamValue() {
        return paramValue;
    }

    public void setParamValue(String paramValue) {
        this.paramValue = paramValue;
    }

    @Override
    public int hashCode() {
        return Objects.hash(
                clusterId,
                volumeId,
                paramName,
                paramValue
        );
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof GlusterVolumeSnapshotConfig)) {
            return false;
        }

        GlusterVolumeSnapshotConfig other = (GlusterVolumeSnapshotConfig) obj;
        return Objects.equals(clusterId, other.clusterId)
                && Objects.equals(volumeId, other.volumeId)
                && Objects.equals(paramName, other.paramName)
                && Objects.equals(paramValue, other.paramValue);
    }

    @Override
    public String toString() {
        return ToStringBuilder.forInstance(this)
                .append("clusterId", getClusterId())
                .append("volumeId", getVolumeId())
                .append("paramName", getParamName())
                .append("paramValue", getParamValue())
                .build();
    }
}
