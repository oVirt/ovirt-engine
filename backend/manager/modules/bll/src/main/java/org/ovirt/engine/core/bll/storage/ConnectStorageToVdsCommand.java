package org.ovirt.engine.core.bll.storage;

import org.ovirt.engine.core.bll.context.CommandContext;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.ovirt.engine.core.bll.Backend;
import org.ovirt.engine.core.bll.InternalCommandAttribute;
import org.ovirt.engine.core.common.action.StorageServerConnectionParametersBase;
import org.ovirt.engine.core.common.businessentities.StorageServerConnections;
import org.ovirt.engine.core.common.businessentities.StorageType;
import org.ovirt.engine.core.common.errors.VdcFault;
import org.ovirt.engine.core.common.utils.Pair;
import org.ovirt.engine.core.common.vdscommands.StorageServerConnectionManagementVDSParameters;
import org.ovirt.engine.core.common.vdscommands.VDSCommandType;
import org.ovirt.engine.core.compat.Guid;

@InternalCommandAttribute
public class ConnectStorageToVdsCommand<T extends StorageServerConnectionParametersBase> extends
        StorageServerConnectionCommandBase<T> {
    public ConnectStorageToVdsCommand(T parameters, CommandContext cmdContext) {
        super(parameters, cmdContext);
    }

    public ConnectStorageToVdsCommand(T parameters) {
        this(parameters, null);
    }

    @Override
    protected void executeCommand() {
        Pair<Boolean, Integer> result = connectHostToStorage();
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

    protected Pair<Boolean, Integer> connectHostToStorage() {
        List<StorageServerConnections> connections = Arrays.asList(getConnection());

        if (getConnection().getstorage_type() == StorageType.ISCSI) {
            connections = ISCSIStorageHelper.updateIfaces(connections, getVds().getId());
        }

        Map<String, String> result = (HashMap<String, String>) runVdsCommand(
                VDSCommandType.ConnectStorageServer,
                new StorageServerConnectionManagementVDSParameters(getVds().getId(), Guid.Empty,
                        getConnection().getstorage_type(), connections)).getReturnValue();

        return new Pair<>(StorageHelperDirector.getInstance()
                .getItem(getConnection().getstorage_type())
                .isConnectSucceeded(result, connections),
                Integer.parseInt(result.values().iterator().next()));
    }

    protected boolean isValidStorageConnectionPort(String port) {
         return !StringUtils.isEmpty(port) && StringUtils.isNumeric(port) && Integer.parseInt(port) > 0;
    }
}
