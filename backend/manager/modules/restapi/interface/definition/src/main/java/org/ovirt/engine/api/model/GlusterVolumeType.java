
package org.ovirt.engine.api.model;

public enum GlusterVolumeType {

    DISTRIBUTE,
    REPLICATE,
    DISTRIBUTED_REPLICATE,
    STRIPE,
    DISTRIBUTED_STRIPE;

    public String value() {
        return name().toLowerCase();
    }

    public static GlusterVolumeType fromValue(String v) {
        try {
            return valueOf(v.toUpperCase());
        } catch (IllegalArgumentException e) {
            return null;
        }
    }
}
