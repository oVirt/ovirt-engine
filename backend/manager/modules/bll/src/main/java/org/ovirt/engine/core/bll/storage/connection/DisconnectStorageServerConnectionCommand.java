package org.ovirt.engine.core.bll.storage.connection;

import java.util.ArrayList;
import java.util.Arrays;

import org.ovirt.engine.core.bll.InternalCommandAttribute;
import org.ovirt.engine.core.bll.context.CommandContext;
import org.ovirt.engine.core.common.action.StorageServerConnectionParametersBase;
import org.ovirt.engine.core.common.businessentities.StorageServerConnections;
import org.ovirt.engine.core.common.vdscommands.StorageServerConnectionManagementVDSParameters;
import org.ovirt.engine.core.common.vdscommands.VDSCommandType;
import org.ovirt.engine.core.compat.Guid;

@InternalCommandAttribute
public class DisconnectStorageServerConnectionCommand<T extends StorageServerConnectionParametersBase> extends
        StorageServerConnectionCommandBase<T> {

    public DisconnectStorageServerConnectionCommand(T parameters, CommandContext cmdContext) {
        super(parameters, cmdContext);
    }

    @Override
    protected void executeCommand() {
        setSucceeded(disconnectStorage());
    }

    protected boolean disconnectStorage() {
        return runVdsCommand(
                    VDSCommandType.DisconnectStorageServer,
                        new StorageServerConnectionManagementVDSParameters(getParameters().getVdsId(), Guid.Empty,
                                getParameters().getStorageServerConnection().getStorageType(),
                                new ArrayList<>(Arrays
                                        .asList(new StorageServerConnections[] { getConnection() })))).getSucceeded();
    }

}
