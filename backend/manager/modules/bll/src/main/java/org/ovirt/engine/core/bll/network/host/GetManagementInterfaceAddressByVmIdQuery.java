package org.ovirt.engine.core.bll.network.host;

import org.ovirt.engine.core.bll.QueriesCommandBase;
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
            VM vm = getDbFacade().getVmDao().get(vmId);
            if (vm != null) {
                NGuid vdsId = vm.getRunOnVds();
                if (vdsId != null) {
                    nic =
                        getDbFacade().getInterfaceDao().getManagedInterfaceForVds(vdsId.getValue(),
                                getUserID(),
                                getParameters().isFiltered());
                }
            }
        }

        getQueryReturnValue().setReturnValue(nic == null ? null : nic.getAddress());
    }

}
