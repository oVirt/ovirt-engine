package org.ovirt.engine.api.model;

import org.apache.commons.lang.StringUtils;

public enum WatchdogModel {
    I6300ESB,
    IB700;
    public String value() {
        return this.name().toLowerCase();
    }

    public static WatchdogModel fromValue(String value) {
        try {
            return valueOf(StringUtils.upperCase(value));
        } catch (IllegalArgumentException e) {
            return null;
        }
    }
}
