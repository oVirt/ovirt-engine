package org.ovirt.engine.core.bll.storage;

import java.util.HashMap;

import org.ovirt.engine.core.bll.Backend;
import org.ovirt.engine.core.bll.InternalCommandAttribute;
import org.ovirt.engine.core.bll.NonTransactiveCommandAttribute;
import org.ovirt.engine.core.common.action.HostStoragePoolParametersBase;
import org.ovirt.engine.core.common.vdscommands.StorageServerConnectionManagementVDSParameters;
import org.ovirt.engine.core.common.vdscommands.VDSCommandType;
import org.ovirt.engine.core.common.vdscommands.VDSReturnValue;

@InternalCommandAttribute
@NonTransactiveCommandAttribute
public class DisconnectHostFromStoragePoolServersCommand extends
        ConnectHostToStoragePooServerCommandBase<HostStoragePoolParametersBase> {

    public DisconnectHostFromStoragePoolServersCommand(HostStoragePoolParametersBase parameters) {
        super(parameters);
        setStoragePool(parameters.getStoragePool());
        setVds(parameters.getVds());
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
                        new StorageServerConnectionManagementVDSParameters(getVds().getId(), getStoragePool().getId(),
                                getStoragePool().getStorageType(), getConnections()));
        setSucceeded(vdsReturnValue.getSucceeded());
        if (!vdsReturnValue.getSucceeded()) {
            StorageHelperDirector.getInstance().getItem(getStoragePool().getStorageType())
                    .isConnectSucceeded((HashMap<String, String>) vdsReturnValue.getReturnValue(), getConnections());
        }
        if (getIsoConnections() != null && getIsoConnections().size() != 0) {
            vdsReturnValue = Backend
                    .getInstance()
                    .getResourceManager()
                    .RunVdsCommand(
                            VDSCommandType.DisconnectStorageServer,
                            new StorageServerConnectionManagementVDSParameters(getVds().getId(),
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
                            new StorageServerConnectionManagementVDSParameters(getVds().getId(),
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
}
