package org.ovirt.engine.core.bll;

import javax.inject.Inject;

import org.ovirt.engine.core.bll.context.EngineContext;
import org.ovirt.engine.core.bll.utils.VmDeviceUtils;
import org.ovirt.engine.core.common.queries.IdQueryParameters;

public class GetVirtioScsiControllersQuery<P extends IdQueryParameters> extends QueriesCommandBase<P> {

    @Inject
    private VmDeviceUtils vmDeviceUtils;

    public GetVirtioScsiControllersQuery(P parameters, EngineContext engineContext) {
        super(parameters, engineContext);
    }

    @Override
    protected void executeQueryCommand() {
        getQueryReturnValue().setReturnValue(vmDeviceUtils.getVirtioScsiControllers(getParameters().getId(),
                getUserID(), getParameters().isFiltered()));
    }
}
