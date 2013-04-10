package org.ovirt.engine.core.bll;

import java.util.Collections;
import java.util.List;

import org.ovirt.engine.core.common.businessentities.VmDevice;
import org.ovirt.engine.core.common.businessentities.VmWatchdog;
import org.ovirt.engine.core.common.businessentities.VmWatchdogAction;
import org.ovirt.engine.core.common.businessentities.VmWatchdogType;
import org.ovirt.engine.core.common.queries.IdQueryParameters;
import org.ovirt.engine.core.common.utils.VmDeviceType;
import org.ovirt.engine.core.dao.VmDeviceDAO;

public class GetWatchdogQuery<P extends IdQueryParameters> extends QueriesCommandBase<P> {

    public GetWatchdogQuery(P parameters) {
        super(parameters);
    }

    protected void executeQueryCommand() {
        final List<VmDevice> vmDevices = getVmDeviceDAO().getVmDeviceByVmIdAndType(getParameters().getId(),
                VmDeviceType.WATCHDOG.getName());
        if (vmDevices != null && !vmDevices.isEmpty()) {
            VmDevice device = vmDevices.get(0);
            VmWatchdog watchdog = new VmWatchdog();
            watchdog.setAction(VmWatchdogAction.getByName((String) device.getSpecParams().get("action")));
            watchdog.setModel(VmWatchdogType.getByName((String) device.getSpecParams().get("model")));
            watchdog.setVmId(getParameters().getId());
            watchdog.setId(device.getDeviceId());
            setReturnValue(Collections.singletonList(watchdog));
        } else {
            setReturnValue(Collections.emptyList());
        }
    }

    protected VmDeviceDAO getVmDeviceDAO() {
        return getDbFacade().getVmDeviceDao();
    }

}
