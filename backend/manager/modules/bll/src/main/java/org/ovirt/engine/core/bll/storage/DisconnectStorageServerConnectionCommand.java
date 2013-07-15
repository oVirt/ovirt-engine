package org.ovirt.engine.core.bll.storage;

import java.util.ArrayList;
import java.util.Arrays;
import org.ovirt.engine.core.bll.Backend;
import org.ovirt.engine.core.bll.InternalCommandAttribute;
import org.ovirt.engine.core.common.action.StorageServerConnectionParametersBase;
import org.ovirt.engine.core.common.businessentities.StorageServerConnections;
import org.ovirt.engine.core.common.vdscommands.StorageServerConnectionManagementVDSParameters;
import org.ovirt.engine.core.common.vdscommands.VDSCommandType;

@InternalCommandAttribute
public class DisconnectStorageServerConnectionCommand<T extends StorageServerConnectionParametersBase> extends
        StorageServerConnectionCommandBase<T> {
    public DisconnectStorageServerConnectionCommand(T parameters) {
        super(parameters);
    }

    @Override
    protected void executeCommand() {
        setSucceeded(disconnectStorage());
    }

    protected boolean disconnectStorage() {
        return Backend.getInstance()
               .getResourceManager()
               .RunVdsCommand(
                    VDSCommandType.DisconnectStorageServer,
                        new StorageServerConnectionManagementVDSParameters(getParameters().getVdsId(), getParameters()
                                .getStoragePoolId(), getParameters().getStorageServerConnection().getstorage_type(),
                                new ArrayList<>(Arrays
                                        .asList(new StorageServerConnections[] { getConnection() })))).getSucceeded() ;
    }

}
