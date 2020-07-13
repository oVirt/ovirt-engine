package org.ovirt.engine.core.bll;

import javax.inject.Inject;

import org.apache.commons.lang.StringUtils;
import org.ovirt.engine.core.bll.context.EngineContext;
import org.ovirt.engine.core.bll.utils.VmDeviceUtils;
import org.ovirt.engine.core.common.businessentities.VmDeviceGeneralType;
import org.ovirt.engine.core.common.queries.IdAndChipsetQueryParameters;
import org.ovirt.engine.core.common.utils.VmDeviceType;
import org.ovirt.engine.core.dao.VmDeviceDao;

public class IsVmTemplateConflictsWithChipsetQuery<P extends IdAndChipsetQueryParameters> extends QueriesCommandBase<P> {

    @Inject
    private VmDeviceDao vmDeviceDao;

    @Inject
    private VmDeviceUtils vmDeviceUtils;

    public IsVmTemplateConflictsWithChipsetQuery(P parameters, EngineContext engineContext) {
        super(parameters, engineContext);
    }

    @Override
    protected void executeQueryCommand() {
        switch (getParameters().getChipsetType()) {
            case I440FX:
                getQueryReturnValue().setReturnValue(isConflictsWithI440fx());
                break;
            case Q35:
                getQueryReturnValue().setReturnValue(isConflictsWithQ35());
                break;
            default:
                getQueryReturnValue().setReturnValue(false);
        }
    }

    private boolean isConflictsWithI440fx() {
        var devices = vmDeviceDao.getVmDeviceByVmId(getParameters().getId());
        if (vmDeviceUtils.containsDeviceWithType(devices, VmDeviceGeneralType.CONTROLLER, VmDeviceType.SATA)) {
            return true;
        }
        var cdDevice = vmDeviceUtils.getFirstDeviceWithType(devices, VmDeviceGeneralType.DISK, VmDeviceType.CDROM);
        if (cdDevice != null
                && !StringUtils.isEmpty(cdDevice.getAddress())
                && !cdDevice.getAddress().contains("unit=0")
                && !cdDevice.getAddress().contains("unit=1")) {
            return true;
        }
        return false;
    }

    private boolean isConflictsWithQ35() {
        var devices = vmDeviceDao.getVmDeviceByVmId(getParameters().getId());
        if (vmDeviceUtils.containsDeviceWithType(devices, VmDeviceGeneralType.CONTROLLER, VmDeviceType.IDE)) {
            return true;
        }
        var cdDevice = vmDeviceUtils.getFirstDeviceWithType(devices, VmDeviceGeneralType.DISK, VmDeviceType.CDROM);
        if (cdDevice != null
                && !StringUtils.isEmpty(cdDevice.getAddress())
                && !cdDevice.getAddress().contains("bus=0")) {
            return true;
        }
        return false;
    }

}
