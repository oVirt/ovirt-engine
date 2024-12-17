package org.ovirt.engine.api.restapi.types;

import org.ovirt.engine.core.common.businessentities.TpmSupport;

public class TpmSupportMapper {
    @Mapping(from = TpmSupport.class, to = org.ovirt.engine.api.model.TpmSupport.class)
    public static org.ovirt.engine.api.model.TpmSupport map(TpmSupport entity, TpmSupport template) {
        if (entity == null) {
            return null;
        }

        switch (entity) {
            case SUPPORTED:
                return org.ovirt.engine.api.model.TpmSupport.SUPPORTED;
            case REQUIRED:
                return org.ovirt.engine.api.model.TpmSupport.REQUIRED;
            case UNSUPPORTED:
                return org.ovirt.engine.api.model.TpmSupport.UNSUPPORTED;
            default:
                return null;
        }
    }
}
