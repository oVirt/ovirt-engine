package org.ovirt.engine.api.model;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
public enum QuotaModeType {
    ENABLED,
    DISABLED,
    AUDIT;

    private static final Logger log = LoggerFactory.getLogger(QuotaModeType.class);
    public String value() {
        return name().toLowerCase();
    }

    public static QuotaModeType fromValue(String value) {
        try {
            return valueOf(value.toUpperCase());
        } catch (IllegalArgumentException e) {
            log.error("The value '{}' isn't a valid quota mode.", value);
            log.error("Exception", e);
            return null;
        }
    }
}
