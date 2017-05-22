package org.ovirt.engine.core.bll;

import javax.inject.Inject;

import org.ovirt.engine.core.bll.context.EngineContext;
import org.ovirt.engine.core.common.businessentities.HostDevice;
import org.ovirt.engine.core.common.queries.HostDeviceParameters;
import org.ovirt.engine.core.dao.HostDeviceDao;

public class GetHostDeviceByHostIdAndDeviceNameQuery<P extends HostDeviceParameters> extends QueriesCommandBase<P> {

    public GetHostDeviceByHostIdAndDeviceNameQuery(P parameters, EngineContext engineContext) {
        super(parameters, engineContext);
    }

    @Inject
    HostDeviceDao hostDeviceDao;

    @Override
    protected void executeQueryCommand() {
        HostDevice hostDevice = hostDeviceDao.getHostDeviceByHostIdAndDeviceName(getParameters().getHostId(), getParameters().getDeviceName());
        getQueryReturnValue().setReturnValue(hostDevice);
    }
}
