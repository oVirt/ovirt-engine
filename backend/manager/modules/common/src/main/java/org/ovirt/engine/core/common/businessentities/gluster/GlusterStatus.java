package org.ovirt.engine.core.common.businessentities.gluster;

/**
 * Enum of Gluster Statuses
 *
 * @see GlusterVolumeEntity
 * @see GlusterBrickEntity
 */
public enum GlusterStatus {
    /**
     * Volume is in "started" state, and can be mounted and used by clients. Brick is in Up state, the data can be
     * stored or retrieved from it.
     */
    UP,
    /**
     * Volume needs to be started, for clients to be able to mount and use it. Brick is in Down state, the data cannot
     * be stored or retrieved from it.
     */
    DOWN,
    /**
     * When the gluster status cannot be determined due to host being non-responsive
     */
    UNKNOWN,
    /**
     * When one or more bricks are down in the volume.
     */
    WARNING;
}
