package org.ovirt.engine.core.bll.storage.pool;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import javax.inject.Inject;

import org.ovirt.engine.core.bll.InternalCommandAttribute;
import org.ovirt.engine.core.bll.NonTransactiveCommandAttribute;
import org.ovirt.engine.core.bll.context.CommandContext;
import org.ovirt.engine.core.bll.storage.connection.CINDERStorageHelper;
import org.ovirt.engine.core.common.AuditLogType;
import org.ovirt.engine.core.common.action.HostStoragePoolParametersBase;
import org.ovirt.engine.core.common.businessentities.StorageDomain;
import org.ovirt.engine.core.common.businessentities.StorageDomainStatic;
import org.ovirt.engine.core.common.businessentities.StorageServerConnections;
import org.ovirt.engine.core.common.businessentities.storage.StorageType;
import org.ovirt.engine.core.common.utils.Pair;
import org.ovirt.engine.core.common.vdscommands.StorageServerConnectionManagementVDSParameters;
import org.ovirt.engine.core.common.vdscommands.VDSCommandType;
import org.ovirt.engine.core.common.vdscommands.VDSReturnValue;
import org.ovirt.engine.core.dal.dbbroker.auditloghandling.AuditLogDirector;
import org.ovirt.engine.core.dao.StorageDomainDao;

@InternalCommandAttribute
@NonTransactiveCommandAttribute
public class DisconnectHostFromStoragePoolServersCommand extends
        ConnectHostToStoragePoolServerCommandBase<HostStoragePoolParametersBase> {

    @Inject
    private AuditLogDirector auditLogDirector;
    @Inject
    private CINDERStorageHelper cinderStorageHelper;
    @Inject
    private StorageDomainDao storageDomainDao;

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
            Pair<Boolean, AuditLogType> result = storageHelperDirector.getItem(connectionToType.getKey()).disconnectHostFromStoragePoolServersCommandCompleted(getParameters());
            if (!result.getFirst()) {
                auditLogDirector.log(this, result.getSecond());
            }
        }

        // Unregister libvirt secrets when required (for Cinder storage domains).
        if (cinderStorageHelper.isActiveCinderDomainAvailable(getStoragePool().getId())) {
            unregisterLibvirtSecrets();
        }
    }

    private void disconnectStorageByType(StorageType storageType, List<StorageServerConnections> connections) {
        /*
         * HE SD should only be connected/disconnected by the HE tools, not
         * by the engine.
         */
        Set<String> heIds = storageDomainDao.getHostedEngineStorageDomainIds().stream()
                .map(storageDomainDao::get)
                .map(StorageDomain::getStorageStaticData)
                .map(StorageDomainStatic::getStorage)
                .collect(Collectors.toSet());

        connections = connections.stream()
                .filter(c -> !heIds.contains(c.getId()))
                .collect(Collectors.toList());

        storageHelperDirector.getItem(storageType).prepareDisconnectHostFromStoragePoolServers(getParameters(), connections);
        VDSReturnValue vdsReturnValue = runVdsCommand(
                        VDSCommandType.DisconnectStorageServer,
                        new StorageServerConnectionManagementVDSParameters(getVds().getId(), getStoragePool().getId(),
                                storageType, connections));
        setSucceeded(vdsReturnValue.getSucceeded());
        if (!vdsReturnValue.getSucceeded()) {
            storageHelperDirector.getItem(storageType)
                    .isConnectSucceeded((HashMap<String, String>) vdsReturnValue.getReturnValue(), connections);
        }
    }
}
