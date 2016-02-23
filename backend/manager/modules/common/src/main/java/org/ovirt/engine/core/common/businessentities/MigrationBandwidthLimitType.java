package org.ovirt.engine.core.common.businessentities;

import java.io.Serializable;

public enum MigrationBandwidthLimitType implements Serializable {

    /**
     * <ul>
     *     <li>If QoS of migration network is defined then
     *     {@link org.ovirt.engine.core.common.businessentities.network.HostNetworkQos#outAverageUpperlimit}</li>
     *     <li>Else if {@link org.ovirt.engine.core.common.businessentities.network.NetworkInterface#speed} exists for
     *     both sending and receiving network interfaces then the network saturation constant times min of these link
     *     speeds.
     *     <li>Otherwise use vdsm configuration as in {@link #VDSM_CONFIG}</li>
     * </ul>
     */
    AUTO,

    /**
     * Migration bandwidth controlled by local vdsm configuration on source host.<br/>
     * <code>migration_progress_timeout</code> option
     */
    VDSM_CONFIG,

    /**
     * User defined
     */
    CUSTOM;

    public static final MigrationBandwidthLimitType DEFAULT = AUTO;
}
