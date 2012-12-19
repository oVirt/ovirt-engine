package org.ovirt.engine.core.bll.storage;

import java.util.HashMap;

import org.ovirt.engine.core.bll.Backend;
import org.ovirt.engine.core.bll.InternalCommandAttribute;
import org.ovirt.engine.core.common.action.StoragePoolParametersBase;
import org.ovirt.engine.core.common.businessentities.StoragePoolStatus;
import org.ovirt.engine.core.common.vdscommands.ConnectStorageServerVDSCommandParameters;
import org.ovirt.engine.core.common.vdscommands.VDSCommandType;
import org.ovirt.engine.core.common.vdscommands.VDSReturnValue;
import org.ovirt.engine.core.dal.VdcBllMessages;

@InternalCommandAttribute
public class DisconnectHostFromStoragePoolServersCommand<T extends StoragePoolParametersBase> extends
        ConnectHostToStoragePooServerCommandBase<T> {
    public DisconnectHostFromStoragePoolServersCommand(T parameters) {
        super(parameters);
    }

    @Override
    protected void executeCommand() {
        InitConnectionList();
        // TODO: check if host belong to more than one storage pool
        VDSReturnValue vdsReturnValue = Backend
                .getInstance()
                .getResourceManager()
                .RunVdsCommand(
                        VDSCommandType.DisconnectStorageServer,
                        new ConnectStorageServerVDSCommandParameters(getVds().getId(), getStoragePool().getId(),
                                getStoragePool().getstorage_pool_type(), getConnections()));
        setSucceeded(vdsReturnValue.getSucceeded());
        if (!vdsReturnValue.getSucceeded()) {
            StorageHelperDirector.getInstance().getItem(getStoragePool().getstorage_pool_type())
                    .isConnectSucceeded((HashMap<String, String>) vdsReturnValue.getReturnValue(), getConnections());
        }
        if (getIsoConnections() != null && getIsoConnections().size() != 0) {
            vdsReturnValue = Backend
                    .getInstance()
                    .getResourceManager()
                    .RunVdsCommand(
                            VDSCommandType.DisconnectStorageServer,
                            new ConnectStorageServerVDSCommandParameters(getVds().getId(),
                                    getStoragePool().getId(), getIsoType(), getIsoConnections()));
            setSucceeded(vdsReturnValue.getSucceeded());
            if (!vdsReturnValue.getSucceeded()) {
                StorageHelperDirector.getInstance()
                        .getItem(getIsoType())
                        .isConnectSucceeded((HashMap<String, String>) vdsReturnValue.getReturnValue(),
                                getIsoConnections());
            }
        }
        if (getExportConnections() != null && getExportConnections().size() != 0) {
            vdsReturnValue = Backend
                    .getInstance()
                    .getResourceManager()
                    .RunVdsCommand(
                            VDSCommandType.DisconnectStorageServer,
                            new ConnectStorageServerVDSCommandParameters(getVds().getId(),
                                    getStoragePool().getId(), getExportType(), getExportConnections()));
            setSucceeded(vdsReturnValue.getSucceeded());
            if (!vdsReturnValue.getSucceeded()) {
                StorageHelperDirector.getInstance()
                        .getItem(getExportType())
                        .isConnectSucceeded((HashMap<String, String>) vdsReturnValue.getReturnValue(),
                                getExportConnections());
            }
        }
    }

    @Override
    protected boolean canDoAction() {
        boolean returnValue = checkStoragePool()
                && CheckStoragePoolStatusNotEqual(StoragePoolStatus.Uninitialized,
                                                  VdcBllMessages.ACTION_TYPE_FAILED_STORAGE_POOL_STATUS_ILLEGAL)
                && InitializeVds();
        return returnValue;

    }
}
