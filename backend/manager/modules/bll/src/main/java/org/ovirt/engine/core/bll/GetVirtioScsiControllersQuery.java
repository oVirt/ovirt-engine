package org.ovirt.engine.core.bll;

import org.ovirt.engine.core.bll.utils.VmDeviceUtils;
import org.ovirt.engine.core.common.queries.IdQueryParameters;

public class GetVirtioScsiControllersQuery<P extends IdQueryParameters> extends QueriesCommandBase<P> {

    public GetVirtioScsiControllersQuery(P parameters) {
        super(parameters);
    }

    @Override
    protected void executeQueryCommand() {
        getQueryReturnValue().setReturnValue(VmDeviceUtils.getVirtioScsiControllers(getParameters().getId(),
                getUserID(), getParameters().isFiltered()));
    }
}
