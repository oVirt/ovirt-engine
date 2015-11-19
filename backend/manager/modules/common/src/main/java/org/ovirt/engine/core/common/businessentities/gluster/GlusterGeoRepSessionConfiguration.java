package org.ovirt.engine.core.common.businessentities.gluster;

import java.util.List;
import java.util.Objects;

import org.ovirt.engine.core.common.businessentities.BusinessEntity;
import org.ovirt.engine.core.compat.Guid;

public class GlusterGeoRepSessionConfiguration extends GlusterVolumeOptionInfo implements BusinessEntity<Guid>{

    private static final long serialVersionUID = -6506417314359159692L;

    Guid sessionId;

    List<String> allowedValues;

    @Override
    public Guid getId() {
        return sessionId;
    }

    @Override
    public void setId(Guid id) {
        this.sessionId = id;
    }

    public String getValue() {
        return getDefaultValue();
    }

    public void setValue(String value) {
        setDefaultValue(value);
    }

    public List<String> getAllowedValues() {
        return allowedValues;
    }

    public void setAllowedValues(List<String> allowedValues) {
        this.allowedValues = allowedValues;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof GlusterGeoRepSessionConfiguration)) {
            return false;
        }
        GlusterGeoRepSessionConfiguration other = (GlusterGeoRepSessionConfiguration) obj;
        return super.equals(obj)
                && Objects.equals(sessionId, other.sessionId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(
                super.hashCode(),
                sessionId
        );
    }
}
