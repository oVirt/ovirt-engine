package org.ovirt.engine.core.bll.storage.pool;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.ovirt.engine.core.bll.InternalCommandAttribute;
import org.ovirt.engine.core.bll.NonTransactiveCommandAttribute;
import org.ovirt.engine.core.bll.context.CommandContext;
import org.ovirt.engine.core.bll.storage.connection.CINDERStorageHelper;
import org.ovirt.engine.core.bll.storage.connection.StorageHelperDirector;
import org.ovirt.engine.core.common.AuditLogType;
import org.ovirt.engine.core.common.action.HostStoragePoolParametersBase;
import org.ovirt.engine.core.common.businessentities.StorageServerConnections;
import org.ovirt.engine.core.common.businessentities.storage.StorageType;
import org.ovirt.engine.core.common.utils.Pair;
import org.ovirt.engine.core.common.vdscommands.StorageServerConnectionManagementVDSParameters;
import org.ovirt.engine.core.common.vdscommands.VDSCommandType;
import org.ovirt.engine.core.common.vdscommands.VDSReturnValue;
import org.ovirt.engine.core.dal.dbbroker.auditloghandling.AuditLogableBase;

@InternalCommandAttribute
@NonTransactiveCommandAttribute
public class DisconnectHostFromStoragePoolServersCommand extends
        ConnectHostToStoragePoolServerCommandBase<HostStoragePoolParametersBase> {

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
            Pair<Boolean, AuditLogType> result = StorageHelperDirector.getInstance().getItem(connectionToType.getKey()).disconnectHostFromStoragePoolServersCommandCompleted(getParameters());
            if (!result.getFirst()) {
                auditLogDirector.log(new AuditLogableBase(getParameters().getVdsId()), result.getSecond());
            }
        }

        // Unregister libvirt secrets when required (for Cinder storage domains).
        CINDERStorageHelper CINDERStorageHelper = new CINDERStorageHelper();
        if (CINDERStorageHelper.isActiveCinderDomainAvailable(getStoragePool().getId())) {
            unregisterLibvirtSecrets();
        }
    }

    private void disconnectStorageByType(StorageType storageType, List<StorageServerConnections> connections) {
        StorageHelperDirector.getInstance().getItem(storageType).prepareDisconnectHostFromStoragePoolServers(getParameters(), connections);
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
