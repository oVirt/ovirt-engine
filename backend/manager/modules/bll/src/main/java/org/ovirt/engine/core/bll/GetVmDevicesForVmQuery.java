package org.ovirt.engine.core.bll;

import org.ovirt.engine.core.common.queries.IdQueryParameters;

public class GetVmDevicesForVmQuery<P extends IdQueryParameters> extends QueriesCommandBase<P> {
    public GetVmDevicesForVmQuery(P parameters) {
        super(parameters);
    }

    @Override
    protected void executeQueryCommand() {
        setReturnValue(getDbFacade().getVmDeviceDao().getVmDeviceByVmId(
                getParameters().getId(),
                getUserID(),
                getParameters().isFiltered()));
    }
}
