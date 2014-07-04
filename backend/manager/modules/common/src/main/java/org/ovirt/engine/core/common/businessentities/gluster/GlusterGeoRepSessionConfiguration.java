package org.ovirt.engine.core.common.businessentities.gluster;

import org.ovirt.engine.core.common.businessentities.BusinessEntity;
import org.ovirt.engine.core.common.utils.ObjectUtils;
import org.ovirt.engine.core.compat.Guid;

public class GlusterGeoRepSessionConfiguration implements BusinessEntity<Guid>{

    private static final long serialVersionUID = -6506417314359159692L;

    Guid sessionId;
    String key;
    String value;

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    @Override
    public Guid getId() {
        return sessionId;
    }

    @Override
    public void setId(Guid id) {
        this.sessionId = id;
    }

    @Override
    public boolean equals(Object obj) {
        return (obj != null) && (obj instanceof GlusterGeoRepSessionConfiguration) && ObjectUtils.objectsEqual(getId(), ((GlusterGeoRepSessionConfiguration) obj).getId()) &&
                ObjectUtils.objectsEqual(getKey(), ((GlusterGeoRepSessionConfiguration) obj).getKey()) &&
                ObjectUtils.objectsEqual(getValue(), ((GlusterGeoRepSessionConfiguration) obj).getValue());
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + sessionId.hashCode();
        result = prime * result + key.hashCode();
        result = prime * result + value.hashCode();
        return result;
    }
}
