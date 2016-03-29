package org.ovirt.engine.core.common.businessentities;

import org.ovirt.engine.core.common.businessentities.Provider.AdditionalProperties;

public class ExternalNetworkProviderProperties implements AdditionalProperties {

    private static final long serialVersionUID = -7470940887999871534L;

    private Boolean readOnly;

    public Boolean getReadOnly(){
        return readOnly;
    }

    public void setReadOnly(Boolean entity) {
        this.readOnly = entity;

    }
}
