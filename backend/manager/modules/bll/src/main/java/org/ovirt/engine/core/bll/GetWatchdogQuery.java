package org.ovirt.engine.core.bll;

import java.util.Collections;
import java.util.List;

import org.ovirt.engine.core.bll.context.EngineContext;
import org.ovirt.engine.core.common.businessentities.VmDevice;
import org.ovirt.engine.core.common.businessentities.VmDeviceGeneralType;
import org.ovirt.engine.core.common.businessentities.VmWatchdog;
import org.ovirt.engine.core.common.queries.IdQueryParameters;
import org.ovirt.engine.core.dao.VmDeviceDao;

public class GetWatchdogQuery<P extends IdQueryParameters> extends QueriesCommandBase<P> {

    public GetWatchdogQuery(P parameters) {
        super(parameters);
    }

    public GetWatchdogQuery(P parameters, EngineContext context) {
        super(parameters, context);
    }

    @Override
    protected void executeQueryCommand() {
        final List<VmDevice> vmDevices = getVmDeviceDao().getVmDeviceByVmIdAndType(getParameters().getId(),
                VmDeviceGeneralType.WATCHDOG);
        if (vmDevices != null && !vmDevices.isEmpty()) {
            setReturnValue(Collections.singletonList(new VmWatchdog(vmDevices.get(0))));
        } else {
            setReturnValue(Collections.emptyList());
        }
    }

    protected VmDeviceDao getVmDeviceDao() {
        return getDbFacade().getVmDeviceDao();
    }

}
