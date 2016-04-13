package org.ovirt.engine.api.restapi.types;

import org.ovirt.engine.api.model.SwitchType;

public class SwitchTypeMapper {
    public static org.ovirt.engine.core.common.network.SwitchType mapFromModel(SwitchType switchType) {
        switch (switchType) {
        case LEGACY:
            return org.ovirt.engine.core.common.network.SwitchType.LEGACY;
        case OVS:
            return org.ovirt.engine.core.common.network.SwitchType.OVS;
        default:
            throw new IllegalArgumentException("Unknown switch type value: " + switchType);
        }
    }

    public static SwitchType mapToModel(org.ovirt.engine.core.common.network.SwitchType switchType) {
        if (switchType == null) {
            return null;
        }

        switch (switchType) {
        case LEGACY:
            return SwitchType.LEGACY;
        case OVS:
            return SwitchType.OVS;
        default:
            throw new IllegalArgumentException("Unknown switch type value: " + switchType);
        }
    }
}
