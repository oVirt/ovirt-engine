package org.ovirt.engine.api.model;

import org.apache.commons.lang.StringUtils;

public enum RngSource {
    RANDOM,
    HWRNG;

    public static RngSource fromValue(String value) {
        try {
            return StringUtils.isEmpty(value) ? null : valueOf(value.toUpperCase());
        } catch (IllegalArgumentException e) {
            return null;
        }
    }
}
