package org.ovirt.engine.core.common.businessentities;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;

import org.ovirt.engine.core.compat.NGuid;

/**
 * <code>VmNetworkStatistics</code> defines a type of {@link BaseNetworkStatistics} for instances of
 * {@link VmNetworkInterface}.
 *
 */
@XmlAccessorType(XmlAccessType.NONE)
@XmlType(name = "VmNetworkStatistics")
public class VmNetworkStatistics extends NetworkStatistics {
    private static final long serialVersionUID = -423834938475712247L;

    @XmlElement(name = "VmId")
    private NGuid vmId;

    /**
     * Sets the VM id.
     *
     * @param vmId
     *            the id
     */
    public void setVmId(NGuid vmId) {
        this.vmId = vmId;
    }

    /**
     * Returns the VM id.
     *
     * @return the id
     */
    public NGuid getVmId() {
        return vmId;
    }
}
