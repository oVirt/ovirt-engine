package org.ovirt.engine.core.bll;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import javax.inject.Inject;

import org.ovirt.engine.core.bll.context.EngineContext;
import org.ovirt.engine.core.common.businessentities.VmDevice;
import org.ovirt.engine.core.common.businessentities.VmDeviceGeneralType;
import org.ovirt.engine.core.common.businessentities.VmWatchdog;
import org.ovirt.engine.core.common.queries.IdsQueryParameters;
import org.ovirt.engine.core.common.utils.VmDeviceType;
import org.ovirt.engine.core.dao.VmDeviceDao;

public class GetWatchdogsQuery<P extends IdsQueryParameters> extends QueriesCommandBase<P> {
    @Inject
    private VmDeviceDao vmDeviceDao;

    public GetWatchdogsQuery(P parameters, EngineContext engineContext) {
        super(parameters, engineContext);
    }

    @Override
    protected void executeQueryCommand() {
        final List<VmDevice> vmDevices = vmDeviceDao.getVmDeviceByTypeAndDevice(getParameters().getIds(),
                VmDeviceGeneralType.WATCHDOG,
                VmDeviceType.WATCHDOG.getName(),
                null, false);
        if (vmDevices != null) {
            setReturnValue(vmDevices.stream().map(VmWatchdog::new).collect(Collectors.toList()));
        } else {
            setReturnValue(Collections.emptyList());
        }
    }
}
