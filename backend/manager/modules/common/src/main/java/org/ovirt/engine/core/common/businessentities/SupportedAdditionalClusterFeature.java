package org.ovirt.engine.core.common.businessentities;

import java.util.Objects;

import org.ovirt.engine.core.common.utils.Pair;
import org.ovirt.engine.core.common.utils.ToStringBuilder;
import org.ovirt.engine.core.compat.Guid;

public class SupportedAdditionalClusterFeature implements BusinessEntity<Pair<Guid, Guid>> {

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
        return Objects.hash(
                clusterId,
                feature,
                enabled
        );
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof SupportedAdditionalClusterFeature)) {
            return false;
        }

        SupportedAdditionalClusterFeature other = (SupportedAdditionalClusterFeature) obj;
        return enabled == other.enabled
                && Objects.equals(clusterId, other.clusterId)
                && Objects.equals(feature, other.feature);
    }

    @Override
    public String toString() {
        return ToStringBuilder.forInstance(this)
                .append("feature", getFeature())
                .append("clusterId", getClusterId())
                .append("enabled", isEnabled())
                .build();
    }

    @Override
    public Pair<Guid, Guid> getId() {
        return new Pair<>(getFeature().getId(), getClusterId());
    }

    @Override
    public void setId(Pair<Guid, Guid> id) {
        getFeature().setId(id.getFirst());
        setClusterId(id.getSecond());
    }
}
