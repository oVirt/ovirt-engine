package org.ovirt.engine.core.common.businessentities.gluster;

import org.ovirt.engine.core.common.businessentities.IVdcQueryable;
import org.ovirt.engine.core.common.utils.ObjectUtils;
import org.ovirt.engine.core.compat.Guid;

public class GlusterVolumeSnapshotConfig extends IVdcQueryable {
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
        final int prime = 31;
        int result = 1;
        result = prime * result + (clusterId == null ? 0 : clusterId.hashCode());
        result = prime * result + (volumeId == null ? 0 : volumeId.hashCode());
        result = prime * result + (paramName == null ? 0 : paramName.hashCode());
        result = prime * result + (paramValue == null ? 0 : paramValue.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null || !(obj instanceof GlusterVolumeSnapshotConfig)) {
            return false;
        }

        GlusterVolumeSnapshotConfig config = (GlusterVolumeSnapshotConfig) obj;

        if (!(ObjectUtils.objectsEqual(clusterId, config.getClusterId()))) {
            return false;
        }

        if (!(ObjectUtils.objectsEqual(volumeId, config.getVolumeId()))) {
            return false;
        }

        if (!(ObjectUtils.objectsEqual(paramName, config.getParamName()))) {
            return false;
        }

        if (!(ObjectUtils.objectsEqual(paramValue, config.getParamValue()))) {
            return false;
        }

        return true;
    }
}
