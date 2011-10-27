package org.ovirt.engine.core.vdsbroker.irsbroker;

import org.ovirt.engine.core.compat.*;
import org.ovirt.engine.core.common.businessentities.*;
import org.ovirt.engine.core.common.vdscommands.*;
import org.ovirt.engine.core.dal.dbbroker.*;

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
