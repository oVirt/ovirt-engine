package org.ovirt.engine.core.bll;

import org.ovirt.engine.core.common.businessentities.HostDeviceView;
import org.ovirt.engine.core.common.queries.IdQueryParameters;
import org.ovirt.engine.core.dao.HostDeviceDao;

import javax.inject.Inject;
import java.util.List;

public class GetExtendedVmHostDevicesByVmIdQuery<P extends IdQueryParameters> extends QueriesCommandBase<P> {

    public GetExtendedVmHostDevicesByVmIdQuery(P parameters) {
        super(parameters);
    }

    @Inject
    private HostDeviceDao hostDeviceDao;

    @Override
    protected void executeQueryCommand() {
        List<HostDeviceView> hostDeviceList = hostDeviceDao.getVmExtendedHostDevicesByVmId(getParameters().getId());
        getQueryReturnValue().setReturnValue(hostDeviceList);
    }
}
