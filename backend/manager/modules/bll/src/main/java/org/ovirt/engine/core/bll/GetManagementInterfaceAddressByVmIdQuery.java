package org.ovirt.engine.core.bll;

import org.ovirt.engine.core.common.businessentities.VM;
import org.ovirt.engine.core.common.businessentities.VdsNetworkInterface;
import org.ovirt.engine.core.common.queries.GetVmByVmIdParameters;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.compat.NGuid;

public class GetManagementInterfaceAddressByVmIdQuery<P extends GetVmByVmIdParameters> extends QueriesCommandBase<P> {

    public GetManagementInterfaceAddressByVmIdQuery(P parameters) {
        super(parameters);
    }

    @Override
    protected void executeQueryCommand() {
        VdsNetworkInterface nic = null;
        Guid vmId = getParameters().getId();
        if (vmId != null) {
            VM vm = getDbFacade().getVmDAO().get(vmId);
            if (vm != null) {
                NGuid vdsId = vm.getrun_on_vds();
                if (vdsId != null) {
                    nic =
                        getDbFacade().getInterfaceDAO().getManagedInterfaceForVds(vdsId.getValue(),
                                getUserID(),
                                getParameters().isFiltered());
                }
            }
        }

        getQueryReturnValue().setReturnValue(nic == null ? null : nic.getAddress());
    }

}
