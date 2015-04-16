package org.ovirt.engine.core.common.vdscommands;

import java.util.List;

import org.ovirt.engine.core.common.businessentities.VDS;
import org.ovirt.engine.core.common.utils.ToStringBuilder;

/**
 * This class is for the list verb that supports getting "full" VM data for a given list of VMs
 */
public class FullListVDSCommandParameters extends VdsIdAndVdsVDSCommandParametersBase {

    private List<String> vmIds;

    public FullListVDSCommandParameters(VDS vds, List<String> vmIds) {
        super(vds);
        this.vmIds = vmIds;
    }

    public FullListVDSCommandParameters() {
    }

    public List<String> getVmIds() {
        return vmIds;
    }

    @Override
    protected ToStringBuilder appendAttributes(ToStringBuilder tsb) {
        return super.appendAttributes(tsb)
                .append("vmIds", getVmIds());
    }
}
