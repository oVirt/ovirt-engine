package org.ovirt.engine.core.common.businessentities.gluster;

/**
 * Enum of Gluster Hooks Statuses
 *
 * @see GlusterHookEntity
 */
public enum GlusterHookStatus {
    /**
     * Hook is enabled in the cluster
     */
    ENABLED,

    /**
     * Hook is disabled in the cluster
     */
    DISABLED,

    /**
     * Unknown/Missing hook status
     */
    MISSING;

}
