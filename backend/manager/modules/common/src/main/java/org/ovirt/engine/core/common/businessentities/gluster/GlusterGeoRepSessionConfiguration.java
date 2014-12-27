package org.ovirt.engine.core.common.businessentities.gluster;

import java.util.List;

import org.ovirt.engine.core.common.businessentities.BusinessEntity;
import org.ovirt.engine.core.common.utils.ObjectUtils;
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
        return obj != null && obj instanceof GlusterGeoRepSessionConfiguration
                && ObjectUtils.objectsEqual(getId(), ((GlusterGeoRepSessionConfiguration) obj).getId())
                && super.equals(obj);
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + sessionId.hashCode();
        result = prime * result + super.hashCode();
        return result;
    }
}
