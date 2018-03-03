package org.ovirt.engine.core.vdsbroker.irsbroker;

import javax.inject.Inject;

import org.ovirt.engine.core.common.AuditLogType;
import org.ovirt.engine.core.common.businessentities.StoragePool;
import org.ovirt.engine.core.common.businessentities.StoragePoolStatus;
import org.ovirt.engine.core.common.errors.EngineError;
import org.ovirt.engine.core.common.vdscommands.ResetIrsVDSCommandParameters;
import org.ovirt.engine.core.common.vdscommands.SpmStopVDSCommandParameters;
import org.ovirt.engine.core.common.vdscommands.VDSCommandType;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dao.StoragePoolDao;

public class ResetIrsVDSCommand<P extends ResetIrsVDSCommandParameters> extends IrsBrokerCommand<P> {
    @Inject
    private StoragePoolDao storagePoolDao;

    public ResetIrsVDSCommand(P parameters) {
        super(parameters);
    }

    @Override
    protected void executeVDSCommand() {
        P parameters = getParameters();
        Guid vdsId = parameters.getVdsId();
        if (getParameters().isVdsAlreadyRebooted() ||
                resourceManager
                .runVdsCommand(VDSCommandType.SpmStop,
                        new SpmStopVDSCommandParameters(vdsId, parameters.getStoragePoolId())).getSucceeded()
                || parameters.getIgnoreStopFailed()) {
            if (getParameters().getPreferredSPMId() != null) {
                getCurrentIrsProxy().setPreferredHostId(getParameters().getPreferredSPMId());
            }

            if (getParameters().isVdsAlreadyRebooted()) {
                getCurrentIrsProxy().setFencedIrs(vdsId);
            }

            getCurrentIrsProxy().resetIrs();

            StoragePool pool = storagePoolDao.get(parameters.getStoragePoolId());
            if (pool != null && (pool.getStatus() == StoragePoolStatus.NotOperational)) {
                resourceManager
                        .getEventListener()
                        .storagePoolStatusChange(parameters.getStoragePoolId(), StoragePoolStatus.NonResponsive,
                                AuditLogType.SYSTEM_CHANGE_STORAGE_POOL_STATUS_RESET_IRS, EngineError.ENGINE);
            }
        } else {
            getVDSReturnValue().setSucceeded(false);
        }
    }
}
