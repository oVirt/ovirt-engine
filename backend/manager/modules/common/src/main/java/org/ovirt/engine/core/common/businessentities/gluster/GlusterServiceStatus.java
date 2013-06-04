package org.ovirt.engine.core.common.businessentities.gluster;

/**
 * Enum for status of gluster related services
 */
public enum GlusterServiceStatus {
    RUNNING,
    STOPPED,
    ERROR, // when service status command is failed in vdsm
    NOT_AVAILABLE, // service is not installed in the host
    MIXED, // cluster-wide status, few up, few down
    UNKNOWN, // Couldn't fetch status
    ;
}
