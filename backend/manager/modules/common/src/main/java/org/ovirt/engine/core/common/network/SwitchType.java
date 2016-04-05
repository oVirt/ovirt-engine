package org.ovirt.engine.core.common.network;

import org.ovirt.engine.core.common.FeatureSupported;
import org.ovirt.engine.core.compat.Version;

public enum SwitchType {
    LEGACY("legacy"),
    OVS("ovs");

    private final String optionValue;

    SwitchType(String optionValue) {
        this.optionValue = optionValue;
    }

    public String getOptionValue() {
        return optionValue;
    }

    public static SwitchType parse(String optionValue) {
        if (optionValue == null) {
            return null;
        }

        for (SwitchType switchType : values()) {
            if (switchType.getOptionValue().equals(optionValue)) {
                return switchType;
            }
        }

        throw new IllegalArgumentException("No enum constant for option " + optionValue);
    }

    public static SwitchType getDefaultSwitchType(Version clusterCompatibilityVersion) {
        boolean ovsSupported = FeatureSupported.ovsSupported(clusterCompatibilityVersion);
        return ovsSupported ? SwitchType.OVS : SwitchType.LEGACY;
    }
}
