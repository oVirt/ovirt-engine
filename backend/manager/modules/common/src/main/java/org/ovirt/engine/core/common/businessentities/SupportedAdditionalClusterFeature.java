package org.ovirt.engine.core.common.businessentities;

import java.io.Serializable;

import org.ovirt.engine.core.common.utils.ObjectUtils;
import org.ovirt.engine.core.compat.Guid;

public class SupportedAdditionalClusterFeature implements Serializable {

    private static final long serialVersionUID = -1063480824650271898L;
    private Guid clusterId;
    private boolean enabled;
    private AdditionalFeature feature;

    public SupportedAdditionalClusterFeature() {
    }

    public SupportedAdditionalClusterFeature(Guid clusterId, boolean enabled, AdditionalFeature feature) {
        this.clusterId = clusterId;
        this.setEnabled(enabled);
        this.feature = feature;
    }

    public Guid getClusterId() {
        return clusterId;
    }

    public void setClusterId(Guid clusterId) {
        this.clusterId = clusterId;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public AdditionalFeature getFeature() {
        return feature;
    }

    public void setFeature(AdditionalFeature feature) {
        this.feature = feature;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((clusterId == null) ? 0 : clusterId.hashCode());
        result = prime * result + ((feature == null) ? 0 : feature.hashCode());
        result = prime * result + ((enabled) ? 0 : 1);
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if ((obj != null) && (obj instanceof SupportedAdditionalClusterFeature)) {
            SupportedAdditionalClusterFeature feature = (SupportedAdditionalClusterFeature) obj;
            if (enabled == feature.isEnabled()
                    && ObjectUtils.objectsEqual(getClusterId(), feature.getClusterId())
                    && ObjectUtils.objectsEqual(getFeature(), feature.getFeature())) {
                return true;
            }
        }
        return false;
    }

    @Override
    public String toString() {
        return "SupportedAdditionalClusterFeature [clusterId=" + getClusterId() + ", feature=" + getFeature()
                + ", enabled=" + isEnabled() + "]";
    }

}
