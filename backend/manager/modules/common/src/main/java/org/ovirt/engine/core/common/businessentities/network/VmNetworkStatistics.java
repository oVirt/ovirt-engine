package org.ovirt.engine.core.common.businessentities.network;

import org.ovirt.engine.core.common.businessentities.comparators.BusinessEntityComparator;
import org.ovirt.engine.core.compat.Guid;

/**
 * <code>VmNetworkStatistics</code> defines a type of {@link BaseNetworkStatistics} for instances of
 * {@link VmNetworkInterface}.
 *
 */
public class VmNetworkStatistics extends NetworkStatistics implements Comparable<VmNetworkStatistics> {
    private static final long serialVersionUID = -423834938475712247L;

    private Guid vmId;

    /**
     * Sets the VM id.
     *
     * @param vmId
     *            the id
     */
    public void setVmId(Guid vmId) {
        this.vmId = vmId;
    }

    /**
     * Returns the VM id.
     *
     * @return the id
     */
    public Guid getVmId() {
        return vmId;
    }

    @Override
    public int compareTo(VmNetworkStatistics o) {
        return BusinessEntityComparator.<VmNetworkStatistics, Guid>newInstance().compare(this, o);
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = super.hashCode();
        result = prime * result + ((getVmId() == null) ? 0 : getVmId().hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (!super.equals(obj))
            return false;
        if (!(obj instanceof VmNetworkStatistics))
            return false;
        VmNetworkStatistics other = (VmNetworkStatistics) obj;
        if (getVmId() == null) {
            if (other.getVmId() != null)
                return false;
        } else if (!getVmId().equals(other.getVmId()))
            return false;
        return true;
    }
}
