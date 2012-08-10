package org.ovirt.engine.core.common.businessentities.gluster;

/**
 * Enum of Gluster Peer Statuses
 *
 * @see GlusterServerInfo
 */
public enum PeerStatus {
    /**
     * Peer is connected to other peers
     */
    CONNECTED,

    /**
     * Peer is disconnected from other peers
     */
    DISCONNECTED,

    /**
     * Peer status is unknown
     */
    UNKNOWN;
}
