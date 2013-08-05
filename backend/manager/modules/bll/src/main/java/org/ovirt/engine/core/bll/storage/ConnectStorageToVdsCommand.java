package org.ovirt.engine.core.bll.storage;

import java.util.Arrays;

import org.ovirt.engine.core.bll.Backend;
import org.ovirt.engine.core.bll.InternalCommandAttribute;
import org.ovirt.engine.core.common.action.StorageServerConnectionParametersBase;
import org.ovirt.engine.core.common.businessentities.StorageServerConnections;
import org.ovirt.engine.core.common.errors.VdcFault;
import org.ovirt.engine.core.common.utils.Pair;
import org.ovirt.engine.core.common.vdscommands.StorageServerConnectionManagementVDSParameters;
import org.ovirt.engine.core.common.vdscommands.VDSCommandType;
import org.ovirt.engine.core.compat.Guid;

@InternalCommandAttribute
public class ConnectStorageToVdsCommand<T extends StorageServerConnectionParametersBase> extends
        StorageServerConnectionCommandBase<T> {
    public ConnectStorageToVdsCommand(T parameters) {
        super(parameters);
    }

    @Override
    protected void executeCommand() {
        Pair<Boolean, Integer> result = connect(getVds().getId());
        setSucceeded(result.getFirst());
        if (!result.getFirst()) {
            setErrorMessageAtReturn(result);
        }
    }

    private void setErrorMessageAtReturn(Pair<Boolean, Integer> result) {
        VdcFault fault = new VdcFault();
        fault.setError(result.getSecond());
        if (fault.getError() != null) {
            fault.setMessage(
                    Backend.getInstance()
                            .getVdsErrorsTranslator()
                            .TranslateErrorTextSingle(fault.getError().toString()));
        }
        getReturnValue().setFault(fault);
    }

    protected Pair<Boolean, Integer> connect(Guid vdsId) {
        java.util.HashMap<String, String> result = (java.util.HashMap<String, String>) runVdsCommand(
                        VDSCommandType.ConnectStorageServer,
                        new StorageServerConnectionManagementVDSParameters(vdsId, Guid.Empty,
                                getParameters().getStorageServerConnection().getstorage_type(),
                                new java.util.ArrayList<StorageServerConnections>(java.util.Arrays
                                        .asList(new StorageServerConnections[] { getConnection() }))))
                .getReturnValue();
        return new Pair<Boolean, Integer>(StorageHelperDirector.getInstance()
                .getItem(getParameters().getStorageServerConnection().getstorage_type())
                .isConnectSucceeded(result, Arrays.asList(getConnection())),
                Integer.parseInt(result.values().iterator().next()));
    }
}
