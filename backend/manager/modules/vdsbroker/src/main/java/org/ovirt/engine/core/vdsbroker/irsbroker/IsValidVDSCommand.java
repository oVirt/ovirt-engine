package org.ovirt.engine.core.vdsbroker.irsbroker;

import org.ovirt.engine.core.common.businessentities.StoragePoolStatus;
import org.ovirt.engine.core.common.businessentities.storage_pool;
import org.ovirt.engine.core.common.vdscommands.IrsBaseVDSCommandParameters;
import org.ovirt.engine.core.dal.dbbroker.DbFacade;
import org.ovirt.engine.core.utils.log.LogCompat;
import org.ovirt.engine.core.utils.log.LogFactoryCompat;

public class IsValidVDSCommand<P extends IrsBaseVDSCommandParameters> extends IrsBrokerCommand<P> {
    public IsValidVDSCommand(P parameters) {
        super(parameters);
    }

    @Override
    protected void ExecuteVDSCommand() {
        storage_pool storagePool = DbFacade.getInstance().getStoragePoolDAO().get(
                getParameters().getStoragePoolId());
        try {
            getVDSReturnValue().setReturnValue(
                    storagePool != null && storagePool.getstatus() == StoragePoolStatus.Up
                            && getCurrentIrsProxyData().getIsValid());
        } catch (RuntimeException ex) {
            log.warnFormat("IsValidVDSCommand failed: {0}", ex.getMessage());
            getVDSReturnValue().setReturnValue(false);
        }
    }

    private static LogCompat log = LogFactoryCompat.getLog(IsValidVDSCommand.class);
}
