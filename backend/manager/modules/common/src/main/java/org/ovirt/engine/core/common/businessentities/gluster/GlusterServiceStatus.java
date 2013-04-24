package org.ovirt.engine.core.common.businessentities.gluster;

/**
 * Enum for status of gluster related services
 */
public enum GlusterServiceStatus {
    RUNNING,
    STOPPED,
    FAILED,
    ERROR,
    NOT_INSTALLED,
    MIXED, // cluster-wide status, few up, few down
    ;
}
