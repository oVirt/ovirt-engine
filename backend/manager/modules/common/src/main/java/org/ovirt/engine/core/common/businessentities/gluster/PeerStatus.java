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

    public static PeerStatus fromValue(String value) {
        PeerStatus result = null;
        if (value != null) {
            result = PeerStatus.valueOf(value.toUpperCase());
        }
        return result;
    }
}
