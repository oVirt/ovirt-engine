package org.ovirt.engine.core.bll;

import java.util.List;

import javax.inject.Inject;

import org.ovirt.engine.core.bll.context.EngineContext;
import org.ovirt.engine.core.common.businessentities.HostDeviceView;
import org.ovirt.engine.core.common.queries.IdQueryParameters;
import org.ovirt.engine.core.dao.HostDeviceDao;

public class GetExtendedHostDevicesByHostIdQuery<P extends IdQueryParameters> extends QueriesCommandBase<P> {

    public GetExtendedHostDevicesByHostIdQuery(P parameters, EngineContext engineContext) {
        super(parameters, engineContext);
    }

    @Inject
    private HostDeviceDao hostDeviceDao;

    @Override
    protected void executeQueryCommand() {
        List<HostDeviceView> hostDeviceList = hostDeviceDao.getExtendedHostDevicesByHostId(getParameters().getId());
        getQueryReturnValue().setReturnValue(hostDeviceList);
    }
}
