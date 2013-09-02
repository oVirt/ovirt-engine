package org.ovirt.engine.core.vdsbroker;

import org.ovirt.engine.core.common.businessentities.VM;
import org.ovirt.engine.core.common.businessentities.VMStatus;
import org.ovirt.engine.core.common.vdscommands.MigrateVDSCommandParameters;
import org.ovirt.engine.core.common.vdscommands.VDSReturnValue;
import org.ovirt.engine.core.dal.dbbroker.DbFacade;
import org.ovirt.engine.core.utils.log.Log;
import org.ovirt.engine.core.utils.log.LogFactory;
import org.ovirt.engine.core.utils.transaction.TransactionMethod;
import org.ovirt.engine.core.utils.transaction.TransactionSupport;
import org.ovirt.engine.core.vdsbroker.vdsbroker.MigrateBrokerVDSCommand;

public class MigrateVDSCommand<P extends MigrateVDSCommandParameters> extends VdsIdVDSCommandBase<P> {
    public MigrateVDSCommand(P parameters) {
        super(parameters);
    }

    @Override
    protected void executeVdsIdCommand() {
        MigrateVDSCommandParameters parameters = getParameters();
        if (_vdsManager != null) {
            VMStatus retval;
            MigrateBrokerVDSCommand<MigrateVDSCommandParameters> command =
                    new MigrateBrokerVDSCommand<MigrateVDSCommandParameters>(parameters);
            command.execute();
            VDSReturnValue vdsReturnValue = command.getVDSReturnValue();

            final VM vm = DbFacade.getInstance().getVmDao().get(parameters.getVmId());

            if (vdsReturnValue.getSucceeded()) {
                retval = VMStatus.MigratingFrom;
                ResourceManager.getInstance().InternalSetVmStatus(vm, VMStatus.MigratingFrom);
                    vm.setMigratingToVds(parameters.getDstVdsId());
                    ResourceManager.getInstance().AddAsyncRunningVm(parameters.getVmId());
            } else {
                retval = vm.getStatus();
                log.error("VDS::migrate:: Failed Vm migration");
                getVDSReturnValue().setSucceeded(false);
                getVDSReturnValue().setVdsError(vdsReturnValue.getVdsError());
                getVDSReturnValue().setExceptionString(vdsReturnValue.getExceptionString());
                getVDSReturnValue().setExceptionObject(vdsReturnValue.getExceptionObject());
            }

            TransactionSupport.executeInNewTransaction(new TransactionMethod<Void>() {
                @Override
                public Void runInTransaction() {
                    DbFacade.getInstance().getVmDynamicDao().update(vm.getDynamicData());
                    return null;
                }
            });

            getVDSReturnValue().setReturnValue(retval);
        } else {
            getVDSReturnValue().setSucceeded(false);
        }
    }

    private static Log log = LogFactory.getLog(MigrateVDSCommand.class);
}
