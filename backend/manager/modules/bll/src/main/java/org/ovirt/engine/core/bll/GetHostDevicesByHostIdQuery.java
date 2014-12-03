package org.ovirt.engine.core.bll;

import org.ovirt.engine.core.common.businessentities.HostDevice;
import org.ovirt.engine.core.common.queries.IdQueryParameters;
import org.ovirt.engine.core.dal.dbbroker.DbFacade;

import javax.inject.Inject;
import java.util.List;

public class GetHostDevicesByHostIdQuery<P extends IdQueryParameters> extends QueriesCommandBase<P> {

    public GetHostDevicesByHostIdQuery(P parameters) {
        super(parameters);
    }

    @Inject
    DbFacade dbFacade;

    @Override
    protected void executeQueryCommand() {
        List<HostDevice> hostDevices = dbFacade.getHostDeviceDao().getHostDevicesByHostId(getParameters().getId());
        getQueryReturnValue().setReturnValue(hostDevices);
    }
}
