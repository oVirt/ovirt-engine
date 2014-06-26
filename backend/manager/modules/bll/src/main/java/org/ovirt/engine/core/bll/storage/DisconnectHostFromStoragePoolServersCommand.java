package org.ovirt.engine.core.bll.storage;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.ovirt.engine.core.bll.InternalCommandAttribute;
import org.ovirt.engine.core.bll.NonTransactiveCommandAttribute;
import org.ovirt.engine.core.bll.context.CommandContext;
import org.ovirt.engine.core.common.action.HostStoragePoolParametersBase;
import org.ovirt.engine.core.common.businessentities.StorageServerConnections;
import org.ovirt.engine.core.common.businessentities.StorageType;
import org.ovirt.engine.core.common.vdscommands.StorageServerConnectionManagementVDSParameters;
import org.ovirt.engine.core.common.vdscommands.VDSCommandType;
import org.ovirt.engine.core.common.vdscommands.VDSReturnValue;

@InternalCommandAttribute
@NonTransactiveCommandAttribute
public class DisconnectHostFromStoragePoolServersCommand extends
        ConnectHostToStoragePoolServerCommandBase<HostStoragePoolParametersBase> {

    // Required as invoked not only by command
    public DisconnectHostFromStoragePoolServersCommand(HostStoragePoolParametersBase parameters) {
        this(parameters, null);
    }

    public DisconnectHostFromStoragePoolServersCommand(HostStoragePoolParametersBase parameters, CommandContext cmdContext) {
        super(parameters, cmdContext);
        setStoragePool(parameters.getStoragePool());
        setVds(parameters.getVds());
    }

    @Override
    protected void executeCommand() {
        initConnectionList(true);

        for (Map.Entry<StorageType, List<StorageServerConnections>> connectionToType : getConnectionsTypeMap().entrySet()) {
            disconnectStorageByType(connectionToType.getKey(), connectionToType.getValue());
        }
    }

    private void disconnectStorageByType(StorageType storageType, List<StorageServerConnections> connections) {
        VDSReturnValue vdsReturnValue = runVdsCommand(
                        VDSCommandType.DisconnectStorageServer,
                        new StorageServerConnectionManagementVDSParameters(getVds().getId(), getStoragePool().getId(),
                                storageType, connections));
        setSucceeded(vdsReturnValue.getSucceeded());
        if (!vdsReturnValue.getSucceeded()) {
            StorageHelperDirector.getInstance().getItem(storageType)
                    .isConnectSucceeded((HashMap<String, String>) vdsReturnValue.getReturnValue(), connections);
        }
    }
}
