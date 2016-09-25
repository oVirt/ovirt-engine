package org.ovirt.engine.core.vdsbroker;

import javax.inject.Inject;

import org.ovirt.engine.core.common.businessentities.VMStatus;
import org.ovirt.engine.core.common.businessentities.VmDynamic;
import org.ovirt.engine.core.common.vdscommands.HibernateVDSCommandParameters;
import org.ovirt.engine.core.common.vdscommands.VDSCommandType;
import org.ovirt.engine.core.common.vdscommands.VDSReturnValue;
import org.ovirt.engine.core.dao.VmDynamicDao;

public class HibernateVDSCommand<P extends HibernateVDSCommandParameters> extends ManagingVmCommand<P> {

    @Inject
    private VmDynamicDao vmDynamicDao;

    public HibernateVDSCommand(P parameters) {
        super(parameters);
    }

    @Override
    protected void executeVmCommand() {
        VDSReturnValue vdsReturnValue = resourceManager.runVdsCommand(
                VDSCommandType.HibernateBroker,
                getParameters());

        if (vdsReturnValue.getSucceeded()) {
            VmDynamic vmDynamic = vmDynamicDao.get(getParameters().getVmId());
            vmDynamic.setStatus(VMStatus.SavingState);
            vmManager.update(vmDynamic);
        } else {
            log.error("Failed to hibernate VM '{}' in VDS = '{}' : error = '{}'",
                    getParameters().getVmId(),
                    getParameters().getVdsId(),
                    vdsReturnValue.getExceptionString());
            getVDSReturnValue().setSucceeded(false);
            getVDSReturnValue().setExceptionString(vdsReturnValue.getExceptionString());
            getVDSReturnValue().setExceptionObject(vdsReturnValue.getExceptionObject());
            getVDSReturnValue().setVdsError(vdsReturnValue.getVdsError());
        }
    }
}
