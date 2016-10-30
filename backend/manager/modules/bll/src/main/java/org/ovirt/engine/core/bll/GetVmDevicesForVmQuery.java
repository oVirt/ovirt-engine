package org.ovirt.engine.core.bll;

import javax.inject.Inject;

import org.ovirt.engine.core.common.queries.IdQueryParameters;
import org.ovirt.engine.core.dao.VmDeviceDao;

public class GetVmDevicesForVmQuery<P extends IdQueryParameters> extends QueriesCommandBase<P> {
    @Inject
    private VmDeviceDao vmDeviceDao;

    public GetVmDevicesForVmQuery(P parameters) {
        super(parameters);
    }

    @Override
    protected void executeQueryCommand() {
        setReturnValue(
                vmDeviceDao.getVmDeviceByVmId(getParameters().getId(), getUserID(), getParameters().isFiltered()));
    }
}
