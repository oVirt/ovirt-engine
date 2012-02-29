package org.ovirt.engine.core.vdsbroker.irsbroker;

import org.ovirt.engine.core.compat.*;
import org.ovirt.engine.core.common.*;
import org.ovirt.engine.core.common.businessentities.*;
import org.ovirt.engine.core.vdsbroker.*;
import org.ovirt.engine.core.common.errors.*;

import org.ovirt.engine.core.common.vdscommands.*;
import org.ovirt.engine.core.dal.dbbroker.*;

public class ResetIrsVDSCommand<P extends ResetIrsVDSCommandParameters> extends IrsBrokerCommand<P> {
    public ResetIrsVDSCommand(P parameters) {
        super(parameters);
    }

    @Override
    protected void ExecuteVDSCommand() {
        P parameters = getParameters();
        if (StringHelper.EqOp(parameters.getHostName(), getCurrentIrsProxyData().getmCurrentIrsHost()) ||
                StringHelper.isNullOrEmpty(getCurrentIrsProxyData().getmCurrentIrsHost())) {
            Guid vdsId = parameters.getVdsId();
            if (ResourceManager
                    .getInstance()
                    .runVdsCommand(VDSCommandType.SpmStop,
                            new SpmStopVDSCommandParameters(vdsId, parameters.getStoragePoolId())).getSucceeded()
                    || parameters.getIgnoreStopFailed()) {
                getCurrentIrsProxyData().ResetIrs();
                storage_pool pool = DbFacade.getInstance().getStoragePoolDAO().get(parameters.getStoragePoolId());
                if (pool != null && (pool.getstatus() == StoragePoolStatus.NotOperational)) {
                    ResourceManager
                            .getInstance()
                            .getEventListener()
                            .storagePoolStatusChange(parameters.getStoragePoolId(), StoragePoolStatus.Problematic,
                                    AuditLogType.SYSTEM_CHANGE_STORAGE_POOL_STATUS_RESET_IRS, VdcBllErrors.ENGINE);
                }
            } else {
                getVDSReturnValue().setSucceeded(false);
            }
        }
    }
}
