package org.ovirt.engine.core.vdsbroker.irsbroker;

import org.ovirt.engine.core.common.businessentities.StoragePoolStatus;
import org.ovirt.engine.core.common.businessentities.storage_pool;
import org.ovirt.engine.core.common.vdscommands.IrsBaseVDSCommandParameters;
import org.ovirt.engine.core.dal.dbbroker.DbFacade;
import org.ovirt.engine.core.utils.log.Log;
import org.ovirt.engine.core.utils.log.LogFactory;

public class IsValidVDSCommand<P extends IrsBaseVDSCommandParameters> extends IrsBrokerCommand<P> {
    public IsValidVDSCommand(P parameters) {
        super(parameters);
    }

    @Override
    protected void ExecuteVDSCommand() {
        storage_pool storagePool = DbFacade.getInstance().getStoragePoolDao().get(
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

    private static Log log = LogFactory.getLog(IsValidVDSCommand.class);
}
