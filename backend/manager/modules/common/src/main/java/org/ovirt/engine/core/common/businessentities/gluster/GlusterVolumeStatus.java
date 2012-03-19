package org.ovirt.engine.core.common.businessentities.gluster;

/**
 * Enum of Gluster Volume Statuses
 *
 * @see GlusterVolumeEntity
 */
public enum GlusterVolumeStatus {
    /**
     * Volume is in "started" state, and can be mounted and used by clients
     */
    UP,
    /**
     * Volume needs to be started, for clients to be able to mount and use it
     */
    DOWN;
}
