package org.ovirt.engine.api.model;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public enum OpenstackVolumeAuthenticationKeyUsageType {

    CEPH;

    private static final Logger log = LoggerFactory.getLogger(OpenstackVolumeAuthenticationKeyUsageType.class);

    public String value() {
        return name().toLowerCase();
    }

    public static OpenstackVolumeAuthenticationKeyUsageType fromValue(String v) {
        try {
            return valueOf(v.toUpperCase());
        } catch (IllegalArgumentException e) {
            log.error("The value {} isn't a valid Open Stack volume authentication key usage", v);
            log.error("Exception", e);
            return null;
        }
    }
}
