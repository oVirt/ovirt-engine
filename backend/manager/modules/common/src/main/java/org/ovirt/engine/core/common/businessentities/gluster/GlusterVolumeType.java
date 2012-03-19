package org.ovirt.engine.core.common.businessentities.gluster;

/**
 * Enum for Gluster Volume types.
 * @see GlusterVolumeEntity
 */
public enum GlusterVolumeType {
    /**
     * Files distributed across all servers of the cluster
     */
    DISTRIBUTE,
    /**
     * Files replicated on every server of the cluster
     */
    REPLICATE,
    /**
     * Files replicated across all servers of a replica set (defined by {@link GlusterVolumeEntity#getReplicaCount()} and
     * brick order), and distributed across all replica sets of the volume
     */
    DISTRIBUTED_REPLICATE,
    /**
     * Files striped across all servers of the cluster
     */
    STRIPE,
    /**
     * Files striped across all servers of a stripe set (defined by {@link GlusterVolumeEntity#getStripeCount()} and
     * brick order), and distributed across all stripe sets of the volume
     */
    DISTRIBUTED_STRIPE;
}
