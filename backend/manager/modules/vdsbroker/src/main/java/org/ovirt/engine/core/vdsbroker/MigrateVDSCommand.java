package org.ovirt.engine.core.vdsbroker;

import javax.inject.Inject;

import org.ovirt.engine.core.common.businessentities.VMStatus;
import org.ovirt.engine.core.common.businessentities.VmDynamic;
import org.ovirt.engine.core.common.vdscommands.MigrateVDSCommandParameters;
import org.ovirt.engine.core.common.vdscommands.VDSCommandType;
import org.ovirt.engine.core.common.vdscommands.VDSReturnValue;
import org.ovirt.engine.core.dao.VmDynamicDao;

public class MigrateVDSCommand<P extends MigrateVDSCommandParameters> extends ManagingVmCommand<P> {

    @Inject
    private VmDynamicDao vmDynamicDao;

    public MigrateVDSCommand(P parameters) {
        super(parameters);
    }

    @Override
    protected void executeVmCommand() {
        VDSReturnValue vdsReturnValue = resourceManager.runVdsCommand(
                VDSCommandType.MigrateBroker,
                getParameters());
        VmDynamic vmDynamic = vmDynamicDao.get(getParameters().getVmId());

        if (vdsReturnValue.getSucceeded()) {
            resourceManager.addAsyncRunningVm(getParameters().getVmId());
            vmDynamic.setStatus(VMStatus.MigratingFrom);
            vmDynamic.setMigratingToVds(getParameters().getDstVdsId());
            vmManager.update(vmDynamic);
            getVDSReturnValue().setReturnValue(VMStatus.MigratingFrom);
        } else {
            log.error("Failed to migrate VM '{}' in VDS = '{}' , error = '{}'",
                    getParameters().getVmId(),
                    getParameters().getVdsId(),
                    vdsReturnValue.getExceptionString());
            getVDSReturnValue().setSucceeded(false);
            getVDSReturnValue().setReturnValue(vmDynamic.getStatus());
            getVDSReturnValue().setVdsError(vdsReturnValue.getVdsError());
            getVDSReturnValue().setExceptionString(vdsReturnValue.getExceptionString());
            getVDSReturnValue().setExceptionObject(vdsReturnValue.getExceptionObject());
        }
    }

}
