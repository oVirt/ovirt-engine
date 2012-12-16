package org.ovirt.engine.core.bll.storage;

import java.util.List;

import org.ovirt.engine.core.bll.Backend;
import org.ovirt.engine.core.bll.InternalCommandAttribute;
import org.ovirt.engine.core.bll.NonTransactiveCommandAttribute;
import org.ovirt.engine.core.common.action.StoragePoolParametersBase;
import org.ovirt.engine.core.common.businessentities.StorageType;
import org.ovirt.engine.core.common.businessentities.StorageServerConnections;
import org.ovirt.engine.core.common.vdscommands.ConnectStorageServerVDSCommandParameters;
import org.ovirt.engine.core.common.vdscommands.VDSCommandType;
import org.ovirt.engine.core.dal.VdcBllMessages;

/**
 * Connect host to all Storage server connections in Storage pool. We
 * considering that connection failed only if data domains failed to connect. If
 * Iso/Export domains failed to connect - only log it.
 */
@NonTransactiveCommandAttribute
@InternalCommandAttribute
public class ConnectHostToStoragePoolServersCommand<T extends StoragePoolParametersBase> extends
        ConnectHostToStoragePooServerCommandBase<T> {

    public ConnectHostToStoragePoolServersCommand(T parameters) {
        super(parameters);
    }

    @Override
    protected void executeCommand() {
        setSucceeded(ConnectStorageServer(getStoragePool().getstorage_pool_type(), getConnections()));

        if (getNeedToConnectIso()) {
            if (!ConnectStorageServer(getIsoType(), getIsoConnections())) {
                log.infoFormat("Failed to connect host {0} to StoragePool {1} Iso domain/s connections", getVds()
                        .getvds_name(), getStoragePool().getname());
            }
        }
        if (getNeedToConnectExport()) {
            if (!ConnectStorageServer(getExportType(), getExportConnections())) {
                log.infoFormat("Failed to connect host {0} to StoragePool {1} Export domain/s connections", getVds()
                        .getvds_name(), getStoragePool().getname());
            }
        }
    }

    @Override
    protected boolean canDoAction() {
        boolean returnValue = true;
        InitConnectionList();
        if (!ValidConnection(getStoragePool().getstorage_pool_type(), getConnections())) {
            addCanDoActionMessage(VdcBllMessages.ACTION_TYPE_FAILED_STORAGE_CONNECTION);
            returnValue = false;
        } else {
            if (getIsoConnections() != null && getIsoConnections().size() != 0) {
                setNeedToConnectIso(ValidConnection(getIsoType(), getIsoConnections()));
                if (!getNeedToConnectIso()) {
                    log.infoFormat(
                            "Failed to validated connections for host {0} to StoragePool {1} Iso domain/s connections",
                            getVds().getvds_name(),
                            getStoragePool().getname());
                }
            }
            if (getExportConnections() != null && getExportConnections().size() != 0) {
                setNeedToConnectExport(ValidConnection(getExportType(), getExportConnections()));
                if (!getNeedToConnectExport()) {
                    log.infoFormat(
                            "Failed to validated connections for host {0} to StoragePool {1} Export domain/s connections",
                            getVds().getvds_name(),
                            getStoragePool().getname());
                }
            }
        }
        return returnValue;
    }

    protected boolean ConnectStorageServer(StorageType type, List<StorageServerConnections> connections) {
        boolean connectSucceeded = true;
        if (connections != null && connections.size() > 0) {
            java.util.HashMap<String, String> retValues = (java.util.HashMap<String, String>) Backend
                    .getInstance()
                    .getResourceManager()
                    .RunVdsCommand(
                            VDSCommandType.ConnectStorageServer,
                            new ConnectStorageServerVDSCommandParameters(getVds().getId(),
                                    getStoragePool().getId(), type, connections)).getReturnValue();
            connectSucceeded =
                    StorageHelperDirector.getInstance().getItem(type).IsConnectSucceeded(retValues, connections);
            log.infoFormat("Host {0} storage connection was {1} ", getVds().getvds_name(),
                    connectSucceeded ? "succeeded" : "failed");
        }
        return connectSucceeded;
    }

    protected boolean ValidConnection(StorageType type, List<StorageServerConnections> connections) {
        return (connections != null && (connections.isEmpty() || StorageHelperDirector.getInstance().getItem(type)
                .ValidateStoragePoolConnectionsInHost(getVds(), connections, getStoragePool().getId())));
    }

}
