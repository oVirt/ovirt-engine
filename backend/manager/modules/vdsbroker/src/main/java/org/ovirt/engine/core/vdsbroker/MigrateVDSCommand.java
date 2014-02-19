package org.ovirt.engine.core.vdsbroker;

import org.ovirt.engine.core.common.businessentities.VM;
import org.ovirt.engine.core.common.businessentities.VMStatus;
import org.ovirt.engine.core.common.vdscommands.MigrateVDSCommandParameters;
import org.ovirt.engine.core.common.vdscommands.VDSReturnValue;
import org.ovirt.engine.core.dal.dbbroker.DbFacade;
import org.ovirt.engine.core.dao.VmDAO;
import org.ovirt.engine.core.dao.VmDynamicDAO;
import org.ovirt.engine.core.utils.log.Log;
import org.ovirt.engine.core.utils.log.LogFactory;
import org.ovirt.engine.core.vdsbroker.vdsbroker.MigrateBrokerVDSCommand;

public class MigrateVDSCommand<P extends MigrateVDSCommandParameters> extends VdsIdVDSCommandBase<P> {
    public MigrateVDSCommand(P parameters) {
        super(parameters);
    }

    @Override
    protected void executeVdsIdCommand() {
        if (_vdsManager == null) {
            getVDSReturnValue().setSucceeded(false);
            return;
        }

        MigrateBrokerVDSCommand<?> command = new MigrateBrokerVDSCommand<>(getParameters());
        command.execute();
        VDSReturnValue vdsReturnValue = command.getVDSReturnValue();

        VM vm = getVmDao().get(getParameters().getVmId());

        if (vdsReturnValue.getSucceeded()) {
            ResourceManager.getInstance().AddAsyncRunningVm(getParameters().getVmId());

            ResourceManager.getInstance().InternalSetVmStatus(vm, VMStatus.MigratingFrom);
            vm.setMigratingToVds(getParameters().getDstVdsId());
            getVmDynamicDAO().update(vm.getDynamicData());

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

    private VmDynamicDAO getVmDynamicDAO() {
        return DbFacade.getInstance().getVmDynamicDao();
    }

    private VmDAO getVmDao() {
        return DbFacade.getInstance().getVmDao();
    }

    private static Log log = LogFactory.getLog(MigrateVDSCommand.class);
}
