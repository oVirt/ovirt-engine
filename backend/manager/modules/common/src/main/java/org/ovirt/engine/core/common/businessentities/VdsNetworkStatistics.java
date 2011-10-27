package org.ovirt.engine.core.common.businessentities;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;

import org.ovirt.engine.core.compat.Guid;

/**
 * <code>VdsNetworkStatistics</code> defines a type of {@link BaseNetworkStatistics} for instances of
 * {@link VdsNetworkInterface}.
 *
 */
@XmlAccessorType(XmlAccessType.NONE)
@XmlType(name = "VdsNetworkStatistics")
public class VdsNetworkStatistics extends NetworkStatistics {
    private static final long serialVersionUID = 1627744622924441184L;

    @XmlElement(name = "VdsId")
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
