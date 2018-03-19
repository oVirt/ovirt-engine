package org.ovirt.engine.core.bll;

import java.util.stream.Collectors;

import javax.inject.Inject;

import org.ovirt.engine.core.bll.context.EngineContext;
import org.ovirt.engine.core.common.businessentities.VmDeviceGeneralType;
import org.ovirt.engine.core.common.businessentities.VmHostDevice;
import org.ovirt.engine.core.common.queries.IdQueryParameters;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dao.VmDeviceDao;

public class GetVmHostDevicesQuery<P extends IdQueryParameters> extends QueriesCommandBase<P> {

    @Inject
    VmDeviceDao vmDeviceDao;

    public GetVmHostDevicesQuery(P parameters, EngineContext engineContext) {
        super(parameters, engineContext);
    }

    @Override
    protected void executeQueryCommand() {
        Guid vmId = getParameters().getId();
        setReturnValue(vmDeviceDao.getVmDeviceByVmIdAndType(vmId, VmDeviceGeneralType.HOSTDEV)
                .stream()
                .map(VmHostDevice::new)
                .collect(Collectors.toList()));
    }
}
