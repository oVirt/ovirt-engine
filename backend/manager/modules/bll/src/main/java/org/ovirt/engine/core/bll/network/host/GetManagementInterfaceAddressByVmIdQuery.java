package org.ovirt.engine.core.bll.network.host;

import javax.inject.Inject;

import org.ovirt.engine.core.bll.QueriesCommandBase;
import org.ovirt.engine.core.bll.context.EngineContext;
import org.ovirt.engine.core.common.businessentities.VM;
import org.ovirt.engine.core.common.businessentities.network.VdsNetworkInterface;
import org.ovirt.engine.core.common.queries.IdQueryParameters;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dao.VmDao;
import org.ovirt.engine.core.dao.network.InterfaceDao;

public class GetManagementInterfaceAddressByVmIdQuery<P extends IdQueryParameters> extends QueriesCommandBase<P> {

    @Inject
    private VmDao vmDao;

    @Inject
    private InterfaceDao interfaceDao;

    public GetManagementInterfaceAddressByVmIdQuery(P parameters, EngineContext engineContext) {
        super(parameters, engineContext);
    }

    @Override
    protected void executeQueryCommand() {
        VdsNetworkInterface nic = null;
        Guid vmId = getParameters().getId();
        if (vmId != null) {
            VM vm = vmDao.get(vmId);
            if (vm != null) {
                Guid vdsId = vm.getRunOnVds();
                if (vdsId != null) {
                    nic = interfaceDao.getManagedInterfaceForVds(vdsId, getUserID(), getParameters().isFiltered());
                }
            }
        }

        getQueryReturnValue().setReturnValue(nic == null ? null : nic.getIpv4Address());
    }

}
