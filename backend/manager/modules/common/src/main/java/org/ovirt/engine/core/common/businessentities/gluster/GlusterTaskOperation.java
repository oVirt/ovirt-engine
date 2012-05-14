package org.ovirt.engine.core.common.businessentities.gluster;

/**
 * Enum of Gluster task operations supported by Gluster Volume tasks
 */
public enum GlusterTaskOperation {
    START,
    PAUSE,
    ABORT,
    STOP,
    STATUS,
    COMMIT;
}
