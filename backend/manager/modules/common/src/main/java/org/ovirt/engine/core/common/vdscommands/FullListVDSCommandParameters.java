package org.ovirt.engine.core.common.vdscommands;

import java.util.List;

import org.ovirt.engine.core.common.utils.ToStringBuilder;
import org.ovirt.engine.core.compat.Guid;

/**
 * This class is for the list verb that supports getting "full" VM data for a given list of VMs
 */
public class FullListVDSCommandParameters extends VdsIdVDSCommandParametersBase {

    private List<String> vmIds;

    public FullListVDSCommandParameters(Guid vdsId, List<String> vmIds) {
        super(vdsId);
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
