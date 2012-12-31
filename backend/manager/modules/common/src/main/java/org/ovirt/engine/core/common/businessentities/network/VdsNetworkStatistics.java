package org.ovirt.engine.core.common.businessentities.network;

import org.ovirt.engine.core.compat.Guid;

/**
 * <code>VdsNetworkStatistics</code> defines a type of {@link BaseNetworkStatistics} for instances of
 * {@link VdsNetworkInterface}.
 *
 */
public class VdsNetworkStatistics extends NetworkStatistics {
    private static final long serialVersionUID = 1627744622924441184L;

    private Guid vdsId;

    /**
     * Sets the VDS instance id.
     *
     * @param vdsId
     *            the id
     */
    public void setVdsId(Guid vdsId) {
        this.vdsId = vdsId;
    }

    /**
     * Returns the VDS instance id.
     *
     * @return the id
     */
    public Guid getVdsId() {
        return vdsId;
    }
}
