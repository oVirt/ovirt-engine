package org.ovirt.engine.core.bll;

import org.ovirt.engine.core.common.businessentities.VmDevice;
import org.ovirt.engine.core.common.businessentities.VmDeviceGeneralType;
import org.ovirt.engine.core.common.businessentities.VmHostDevice;
import org.ovirt.engine.core.common.queries.VmHostDeviceQueryParameters;
import org.ovirt.engine.core.dao.VmDeviceDAO;

import javax.inject.Inject;
import java.util.List;

public class GetVmHostDeviceByVmIdAndDeviceNameQuery<P extends VmHostDeviceQueryParameters> extends QueriesCommandBase<P> {

    @Inject
    VmDeviceDAO vmDeviceDAO;

    public GetVmHostDeviceByVmIdAndDeviceNameQuery(P parameters) {
        super(parameters);
    }

    @Override
    protected void executeQueryCommand() {
        List<VmDevice> vmDevices = vmDeviceDAO.getVmDeviceByVmIdTypeAndDevice(getParameters().getId(),
                VmDeviceGeneralType.HOSTDEV, getParameters().getDeviceName());
        if (vmDevices != null && !vmDevices.isEmpty()) {
            setReturnValue(new VmHostDevice(vmDevices.get(0)));
        }
    }
}
