package org.ovirt.engine.core.common.config;

public enum OptionBehaviour {
    /**
     * value is a Password
     */
    Password,
    /**
     * value is a comma separated string array - for List of String
     */
    CommaSeparatedStringArray,
    /**
     * value is dependent in another value
     */
    ValueDependent,
    /**
     * value is a comma separated version array - for hashset of versions
     */
    CommaSeparatedVersionArray;

    public int getValue() {
        return this.ordinal();
    }

    public static OptionBehaviour forValue(int value) {
        return values()[value];
    }
}
