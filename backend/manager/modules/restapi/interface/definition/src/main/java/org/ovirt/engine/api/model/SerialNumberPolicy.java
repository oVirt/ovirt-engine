package org.ovirt.engine.api.model;

import org.apache.commons.lang.StringUtils;

public enum SerialNumberPolicy {
    HOST, VM, CUSTOM;

    public String value() {
        return name().toLowerCase();
    }

    public static SerialNumberPolicy fromValue(String value) {
        try {
            return StringUtils.isEmpty(value) ? null : valueOf(value.toUpperCase());
        } catch (IllegalArgumentException e) {
            return null;
        }
    }
}
