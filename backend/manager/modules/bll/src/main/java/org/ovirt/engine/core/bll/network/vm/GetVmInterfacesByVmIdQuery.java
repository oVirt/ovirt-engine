package org.ovirt.engine.core.bll.network.vm;

import javax.inject.Inject;

import org.ovirt.engine.core.bll.QueriesCommandBase;
import org.ovirt.engine.core.common.queries.IdQueryParameters;
import org.ovirt.engine.core.dao.network.VmNetworkInterfaceDao;

public class GetVmInterfacesByVmIdQuery<P extends IdQueryParameters> extends QueriesCommandBase<P> {
    @Inject
    private VmNetworkInterfaceDao vmNetworkInterfaceDao;

    public GetVmInterfacesByVmIdQuery(P parameters) {
        super(parameters);
    }

    @Override
    protected void executeQueryCommand() {
        getQueryReturnValue().setReturnValue(
                vmNetworkInterfaceDao.getAllForVm(getParameters().getId(), getUserID(), getParameters().isFiltered()));
    }
}
