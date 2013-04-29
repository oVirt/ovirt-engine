package org.ovirt.engine.core.common.vdscommands;

import java.util.List;

import org.ovirt.engine.core.common.businessentities.VDS;

/**
 * This class is for the list verb that supports getting "full" VM data for a given list of VMs
 */
public class FullListVDSCommandParameters extends VdsIdAndVdsVDSCommandParametersBase {

    private List<String> vmIds;

    public FullListVDSCommandParameters(VDS vds, List<String> vmIds) {
        super(vds);
        this.vmIds = vmIds;
    }

    public List<String> getVmIds() {
        return vmIds;
    }

    @Override
    public String toString() {
        return String.format("%s, vmIds=%s", super.toString(), getVmIds());
    }
}
