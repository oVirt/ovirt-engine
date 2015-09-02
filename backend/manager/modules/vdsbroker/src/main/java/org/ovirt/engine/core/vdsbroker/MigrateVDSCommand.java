package org.ovirt.engine.core.vdsbroker;

import org.ovirt.engine.core.common.businessentities.VM;
import org.ovirt.engine.core.common.businessentities.VMStatus;
import org.ovirt.engine.core.common.vdscommands.MigrateVDSCommandParameters;
import org.ovirt.engine.core.common.vdscommands.VDSCommandType;
import org.ovirt.engine.core.common.vdscommands.VDSReturnValue;
import org.ovirt.engine.core.dal.dbbroker.DbFacade;
import org.ovirt.engine.core.dao.VmDao;
import org.ovirt.engine.core.dao.VmDynamicDao;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MigrateVDSCommand<P extends MigrateVDSCommandParameters> extends ManagingVmCommand<P> {

    private static final Logger log = LoggerFactory.getLogger(MigrateVDSCommand.class);

    public MigrateVDSCommand(P parameters) {
        super(parameters);
    }

    @Override
    protected void executeVmCommand() {
        VDSReturnValue vdsReturnValue = resourceManager.runVdsCommand(VDSCommandType.MigrateBroker, getParameters());
        VM vm = getVmDao().get(getParameters().getVmId());

        if (vdsReturnValue.getSucceeded()) {
            resourceManager.addAsyncRunningVm(getParameters().getVmId());
            resourceManager.internalSetVmStatus(vm, VMStatus.MigratingFrom);
            vm.setMigratingToVds(getParameters().getDstVdsId());
            vmManager.update(vm.getDynamicData());
            getVDSReturnValue().setReturnValue(VMStatus.MigratingFrom);
        } else {
            log.error("Failed Vm migration");
            getVDSReturnValue().setSucceeded(false);
            getVDSReturnValue().setReturnValue(vm.getStatus());
            getVDSReturnValue().setVdsError(vdsReturnValue.getVdsError());
            getVDSReturnValue().setExceptionString(vdsReturnValue.getExceptionString());
            getVDSReturnValue().setExceptionObject(vdsReturnValue.getExceptionObject());
        }
    }

    private VmDynamicDao getVmDynamicDao() {
        return DbFacade.getInstance().getVmDynamicDao();
    }

    private VmDao getVmDao() {
        return DbFacade.getInstance().getVmDao();
    }
}
