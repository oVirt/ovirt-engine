package org.ovirt.engine.core.vdsbroker;

import org.ovirt.engine.core.common.businessentities.VMStatus;
import org.ovirt.engine.core.common.businessentities.VmDynamic;
import org.ovirt.engine.core.common.vdscommands.HibernateVDSCommandParameters;
import org.ovirt.engine.core.common.vdscommands.VDSReturnValue;
import org.ovirt.engine.core.dal.dbbroker.DbFacade;
import org.ovirt.engine.core.utils.transaction.TransactionMethod;
import org.ovirt.engine.core.utils.transaction.TransactionSupport;
import org.ovirt.engine.core.vdsbroker.vdsbroker.HibernateBrokerVDSCommand;

public class HibernateVDSCommand<P extends HibernateVDSCommandParameters> extends VdsIdVDSCommandBase<P> {

    public HibernateVDSCommand(P parameters) {
        super(parameters);
    }

    @Override
    protected void ExecuteVdsIdCommand() {
        if (_vdsManager == null) {
            getVDSReturnValue().setSucceeded(false);
            return;
        }

        VDSReturnValue retVal = runHibernateBrokerVDSCommand();
        if (retVal.getSucceeded()) {
            changeVmStatusToSavingState();
            getVDSReturnValue().setSucceeded(true);
        }
        else {
            log.errorFormat("Failed to hibernate vm '{0}' in vds = {1} : {2}, error = {3}",
                    getParameters().getVmId(), getVds().getId(), getVds().getName(), retVal.getExceptionString());
            getVDSReturnValue().setSucceeded(false);
            getVDSReturnValue().setExceptionString(retVal.getExceptionString());
            getVDSReturnValue().setExceptionObject(retVal.getExceptionObject());
            getVDSReturnValue().setVdsError(retVal.getVdsError());
        }
    }

    private VDSReturnValue runHibernateBrokerVDSCommand() {
        HibernateBrokerVDSCommand<HibernateVDSCommandParameters> command =
                new HibernateBrokerVDSCommand<HibernateVDSCommandParameters>(getParameters());
        command.execute();
        return command.getVDSReturnValue();
    }

    private void changeVmStatusToSavingState() {
        TransactionSupport.executeInNewTransaction(
                new TransactionMethod<Object>() {
                    @Override
                    public Object runInTransaction() {
                        VmDynamic vmDynamic = DbFacade.getInstance().getVmDynamicDao().get(getParameters().getVmId());
                        vmDynamic.setStatus(VMStatus.SavingState);
                        _vdsManager.UpdateVmDynamic(vmDynamic);
                        return null;
                    }
                });
    }
}
