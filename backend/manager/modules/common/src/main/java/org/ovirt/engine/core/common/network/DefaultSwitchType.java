package org.ovirt.engine.core.common.network;

import org.ovirt.engine.core.compat.Version;

public class DefaultSwitchType {
    public static SwitchType getDefaultSwitchType(Version clusterCompatibilityVersion) {
        return SwitchType.LEGACY;
    }
}
