package org.ovirt.engine.core.common.businessentities;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

public enum SsoMethod {
    NONE("none"),
    GUEST_AGENT("guest_agent");

    private final String value;

    SsoMethod(String value) {
        this.value = value;
    }

    @JsonCreator
    public static SsoMethod fromString(String val) {
        for (SsoMethod ssoMethod : SsoMethod.values()) {
            if (ssoMethod.value.equalsIgnoreCase(val)) {
                return ssoMethod;
            }
        }

        return null;
    }

    @JsonValue
    @Override
    public String toString() {
        return value;
    }
}
