package org.ovirt.engine.core.common.network;

import org.ovirt.engine.core.common.FeatureSupported;
import org.ovirt.engine.core.compat.Version;

public class DefaultSwitchType {
    public static SwitchType getDefaultSwitchType(Version clusterCompatibilityVersion) {
        boolean ovsSupported = FeatureSupported.ovsSupported(clusterCompatibilityVersion);
        return ovsSupported ? SwitchType.OVS : SwitchType.LEGACY;
    }
}
