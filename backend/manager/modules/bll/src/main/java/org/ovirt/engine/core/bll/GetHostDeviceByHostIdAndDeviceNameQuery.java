package org.ovirt.engine.core.bll;

import org.ovirt.engine.core.common.businessentities.HostDevice;
import org.ovirt.engine.core.common.queries.HostDeviceParameters;
import org.ovirt.engine.core.dal.dbbroker.DbFacade;

import javax.inject.Inject;

public class GetHostDeviceByHostIdAndDeviceNameQuery<P extends HostDeviceParameters> extends QueriesCommandBase<P> {

    public GetHostDeviceByHostIdAndDeviceNameQuery(P parameters) {
        super(parameters);
    }

    @Inject
    DbFacade dbFacade;

    @Override
    protected void executeQueryCommand() {
        HostDevice hostDevice = dbFacade.getHostDeviceDao().getHostDeviceByHostIdAndDeviceName(getParameters().getHostId(), getParameters().getDeviceName());
        getQueryReturnValue().setReturnValue(hostDevice);
    }
}
