package org.ovirt.engine.core.bll;

import javax.inject.Inject;

import org.ovirt.engine.core.bll.context.EngineContext;
import org.ovirt.engine.core.bll.utils.VmDeviceUtils;
import org.ovirt.engine.core.common.businessentities.VmDeviceGeneralType;
import org.ovirt.engine.core.common.queries.IdQueryParameters;
import org.ovirt.engine.core.common.utils.VmDeviceType;
import org.ovirt.engine.core.dao.VmDeviceDao;

public class IsVmTemplateI440fxQuery<P extends IdQueryParameters> extends QueriesCommandBase<P> {

    @Inject
    private VmDeviceDao vmDeviceDao;

    @Inject
    private VmDeviceUtils vmDeviceUtils;

    public IsVmTemplateI440fxQuery(P parameters, EngineContext engineContext) {
        super(parameters, engineContext);
    }

    @Override
    protected void executeQueryCommand() {
        var devices = vmDeviceDao.getVmDeviceByVmId(getParameters().getId());
        if (vmDeviceUtils.containsDeviceWithType(devices, VmDeviceGeneralType.CONTROLLER, VmDeviceType.IDE)) {
            getQueryReturnValue().setReturnValue(true);
            return;
        }
        var cdDevice = vmDeviceUtils.getFirstDeviceWithType(devices, VmDeviceGeneralType.DISK, VmDeviceType.CDROM);
        if (cdDevice != null && !cdDevice.getAddress().contains("bus=0")) {
            getQueryReturnValue().setReturnValue(true);
            return;
        }
        getQueryReturnValue().setReturnValue(false);
    }

}
