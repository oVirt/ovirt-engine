package org.ovirt.engine.core.bll;

import javax.inject.Inject;

import org.ovirt.engine.core.common.queries.IdQueryParameters;
import org.ovirt.engine.core.dao.VmGuestAgentInterfaceDao;

public class GetVmGuestAgentInterfacesByVmIdQuery<P extends IdQueryParameters>
        extends QueriesCommandBase<P> {

    @Inject
    private VmGuestAgentInterfaceDao vmGuestAgentInterfaceDao;

    public GetVmGuestAgentInterfacesByVmIdQuery(P parameters) {
        super(parameters);
    }

    @Override
    protected void executeQueryCommand() {
        getQueryReturnValue().setReturnValue(vmGuestAgentInterfaceDao
                .getAllForVm(getParameters().getId(), getUserID(), getParameters().isFiltered()));
    }
}
