package org.ovirt.engine.core.bll;

import org.ovirt.engine.core.common.businessentities.HostDevice;
import org.ovirt.engine.core.common.queries.IdQueryParameters;
import org.ovirt.engine.core.dao.HostDeviceDao;

import javax.inject.Inject;
import java.util.List;

public class GetHostDevicesByHostIdQuery<P extends IdQueryParameters> extends QueriesCommandBase<P> {

    public GetHostDevicesByHostIdQuery(P parameters) {
        super(parameters);
    }

    @Inject
    HostDeviceDao hostDeviceDao;

    @Override
    protected void executeQueryCommand() {
        List<HostDevice> hostDevices = hostDeviceDao.getHostDevicesByHostId(getParameters().getId());
        getQueryReturnValue().setReturnValue(hostDevices);
    }
}
