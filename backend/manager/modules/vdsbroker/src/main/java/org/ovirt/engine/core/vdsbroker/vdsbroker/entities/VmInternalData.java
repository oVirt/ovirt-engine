package org.ovirt.engine.core.vdsbroker.vdsbroker.entities;

import java.util.List;

import org.ovirt.engine.core.common.businessentities.VmDynamic;
import org.ovirt.engine.core.common.businessentities.VmGuestAgentInterface;
import org.ovirt.engine.core.common.businessentities.VmStatistics;

/**
 * This class represents the internal data of a VM, including {@link VmDynamic}, {@link VmStatistics} and
 * {@link VmGuestAgentInterface}, to manage data received from the VM's host.
 */
public class VmInternalData {

    private VmDynamic vmDynamic;
    private VmStatistics vmStatistics;
    private List<VmGuestAgentInterface> vmGuestAgentInterfaces;

    public VmInternalData(VmDynamic vmDynamic,
            VmStatistics vmStatistics,
            List<VmGuestAgentInterface> vmGuestAgentInterfaces) {
        this.vmDynamic = vmDynamic;
        this.vmStatistics = vmStatistics;
        this.vmGuestAgentInterfaces = vmGuestAgentInterfaces;
    }

    public VmDynamic getVmDynamic() {
        return vmDynamic;
    }

    public void setVmDynamic(VmDynamic vmDynamic) {
        this.vmDynamic = vmDynamic;
    }

    public VmStatistics getVmStatistics() {
        return vmStatistics;
    }

    public void setVmStatistics(VmStatistics vmStatistics) {
        this.vmStatistics = vmStatistics;
    }

    public List<VmGuestAgentInterface> getVmGuestAgentInterfaces() {
        return vmGuestAgentInterfaces;
    }

    public void setVmGuestAgentInterfaces(List<VmGuestAgentInterface> vmGuestAgentInterfaces) {
        this.vmGuestAgentInterfaces = vmGuestAgentInterfaces;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((vmDynamic == null) ? 0 : vmDynamic.hashCode());
        result = prime * result + ((vmGuestAgentInterfaces == null) ? 0 : vmGuestAgentInterfaces.hashCode());
        result = prime * result + ((vmStatistics == null) ? 0 : vmStatistics.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        VmInternalData other = (VmInternalData) obj;
        if (vmDynamic == null) {
            if (other.vmDynamic != null) {
                return false;
            }
        } else if (!vmDynamic.equals(other.vmDynamic)) {
            return false;
        }
        if (vmGuestAgentInterfaces == null) {
            if (other.vmGuestAgentInterfaces != null) {
                return false;
            }
        } else if (!vmGuestAgentInterfaces.equals(other.vmGuestAgentInterfaces)) {
            return false;
        }
        if (vmStatistics == null) {
            if (other.vmStatistics != null) {
                return false;
            }
        } else if (!vmStatistics.equals(other.vmStatistics)) {
            return false;
        }
        return true;
    }

}
