package org.ovirt.engine.core.common.network;

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

}
