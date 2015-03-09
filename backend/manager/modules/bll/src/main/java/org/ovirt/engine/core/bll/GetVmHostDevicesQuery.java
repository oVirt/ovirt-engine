package org.ovirt.engine.core.bll;

import org.ovirt.engine.core.bll.context.EngineContext;
import org.ovirt.engine.core.common.businessentities.VmDevice;
import org.ovirt.engine.core.common.businessentities.VmDeviceGeneralType;
import org.ovirt.engine.core.common.businessentities.VmHostDevice;
import org.ovirt.engine.core.common.queries.IdQueryParameters;
import org.ovirt.engine.core.dao.VmDeviceDAO;

import javax.inject.Inject;
import java.util.ArrayList;
import java.util.List;

public class GetVmHostDevicesQuery<P extends IdQueryParameters> extends QueriesCommandBase<P> {

    @Inject
    VmDeviceDAO vmDeviceDao;

    public GetVmHostDevicesQuery(P parameters) {
        super(parameters);
    }

    public GetVmHostDevicesQuery(P parameters, EngineContext engineContext) {
        super(parameters, engineContext);
    }

    @Override
    protected void executeQueryCommand() {
        List<VmHostDevice> result = new ArrayList<>();
        List<VmDevice> vmHostDevices = vmDeviceDao.getVmDeviceByVmIdAndType(
                getParameters().getId(), VmDeviceGeneralType.HOSTDEV);

        for (VmDevice device : vmHostDevices) {
            result.add(new VmHostDevice(device));
        }

        setReturnValue(result);
    }
}
