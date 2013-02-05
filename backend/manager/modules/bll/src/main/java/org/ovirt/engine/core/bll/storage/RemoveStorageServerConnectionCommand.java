package org.ovirt.engine.core.bll.storage;

import org.apache.commons.lang.StringUtils;
import org.ovirt.engine.core.bll.NonTransactiveCommandAttribute;
import org.ovirt.engine.core.common.action.StorageServerConnectionParametersBase;

@NonTransactiveCommandAttribute
public class RemoveStorageServerConnectionCommand<T extends StorageServerConnectionParametersBase> extends DisconnectStorageServerConnectionCommand {

    public RemoveStorageServerConnectionCommand(T parameters) {
        super(parameters);
    }

    @Override
    protected void executeCommand() {
        String connectionId = getConnection().getid();
        if(StringUtils.isNotEmpty(connectionId)) {
             getDbFacade().getStorageServerConnectionDao().remove(connectionId);
        }

        //disconnect the connection from vdsm
        disconnectStorage();
        setSucceeded(true);
    }
}
