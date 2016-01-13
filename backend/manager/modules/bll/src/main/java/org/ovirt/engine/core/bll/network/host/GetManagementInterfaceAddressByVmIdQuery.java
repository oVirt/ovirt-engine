package org.ovirt.engine.core.bll.network.host;

import org.ovirt.engine.core.bll.QueriesCommandBase;
import org.ovirt.engine.core.common.businessentities.VM;
import org.ovirt.engine.core.common.businessentities.network.VdsNetworkInterface;
import org.ovirt.engine.core.common.queries.IdQueryParameters;
import org.ovirt.engine.core.compat.Guid;

public class GetManagementInterfaceAddressByVmIdQuery<P extends IdQueryParameters> extends QueriesCommandBase<P> {

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
                Guid vdsId = vm.getRunOnVds();
                if (vdsId != null) {
                    nic =
                        getDbFacade().getInterfaceDao().getManagedInterfaceForVds(vdsId,
                                getUserID(),
                                getParameters().isFiltered());
                }
            }
        }

        getQueryReturnValue().setReturnValue(nic == null ? null : nic.getIpv4Address());
    }

}
