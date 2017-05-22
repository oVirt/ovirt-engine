package org.ovirt.engine.core.bll;

import java.util.List;

import javax.inject.Inject;

import org.ovirt.engine.core.bll.context.EngineContext;
import org.ovirt.engine.core.common.businessentities.HostDevice;
import org.ovirt.engine.core.common.queries.IdQueryParameters;
import org.ovirt.engine.core.dao.HostDeviceDao;

public class GetHostDevicesByHostIdQuery<P extends IdQueryParameters> extends QueriesCommandBase<P> {

    public GetHostDevicesByHostIdQuery(P parameters, EngineContext engineContext) {
        super(parameters, engineContext);
    }

    @Inject
    HostDeviceDao hostDeviceDao;

    @Override
    protected void executeQueryCommand() {
        List<HostDevice> hostDevices = hostDeviceDao.getHostDevicesByHostId(getParameters().getId());
        getQueryReturnValue().setReturnValue(hostDevices);
    }
}
