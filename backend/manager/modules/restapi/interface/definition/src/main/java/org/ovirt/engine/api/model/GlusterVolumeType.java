
package org.ovirt.engine.api.model;

public enum GlusterVolumeType {

    DISTRIBUTE,
    REPLICATE,
    DISTRIBUTED_REPLICATE,
    STRIPE,
    DISTRIBUTED_STRIPE;

    public String value() {
        return name();
    }

    public static GlusterVolumeType fromValue(String v) {
        return valueOf(v);
    }

}
