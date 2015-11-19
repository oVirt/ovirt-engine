package org.ovirt.engine.core.common.businessentities.network;

import java.util.Objects;

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

    public VmNetworkStatistics() {
    }

    public VmNetworkStatistics(VmNetworkStatistics statistics) {
        super(statistics);
        setVmId(statistics.getVmId());
    }
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
        return Objects.hash(
                super.hashCode(),
                vmId
        );
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof VmNetworkStatistics)) {
            return false;
        }
        VmNetworkStatistics other = (VmNetworkStatistics) obj;
        return super.equals(obj)
                && Objects.equals(vmId, other.vmId);
    }
}
