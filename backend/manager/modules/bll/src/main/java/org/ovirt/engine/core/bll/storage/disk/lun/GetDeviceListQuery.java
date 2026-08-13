package org.ovirt.engine.core.bll.storage.disk.lun;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

import javax.inject.Inject;

import org.ovirt.engine.core.bll.QueriesCommandBase;
import org.ovirt.engine.core.bll.context.EngineContext;
import org.ovirt.engine.core.common.businessentities.StorageServerConnections;
import org.ovirt.engine.core.common.businessentities.VDS;
import org.ovirt.engine.core.common.businessentities.VDSStatus;
import org.ovirt.engine.core.common.businessentities.storage.LUNStorageServerConnectionMap;
import org.ovirt.engine.core.common.businessentities.storage.LUNs;
import org.ovirt.engine.core.common.errors.EngineMessage;
import org.ovirt.engine.core.common.queries.GetDeviceListQueryParameters;
import org.ovirt.engine.core.common.vdscommands.GetDeviceListVDSCommandParameters;
import org.ovirt.engine.core.common.vdscommands.VDSCommandType;
import org.ovirt.engine.core.dao.LunDao;
import org.ovirt.engine.core.dao.StorageServerConnectionDao;
import org.ovirt.engine.core.dao.StorageServerConnectionLunMapDao;
import org.ovirt.engine.core.dao.VdsDao;

public class GetDeviceListQuery<P extends GetDeviceListQueryParameters> extends QueriesCommandBase<P> {
    @Inject
    private LunDao lunDao;

    @Inject
    private VdsDao vdsDao;

    @Inject
    private StorageServerConnectionDao storageServerConnectionDao;

    @Inject
    private StorageServerConnectionLunMapDao storageServerConnectionLunMapDao;

    public GetDeviceListQuery(P parameters, EngineContext engineContext) {
        super(parameters, engineContext);
    }

    @Override
    protected void executeQueryCommand() {
        if (getParameters().isValidateHostStatus() && !isActiveVds()) {
            return;
        }
        List<LUNs> returnValue = new ArrayList<>();
        // Get Device List
        GetDeviceListVDSCommandParameters parameters =
                new GetDeviceListVDSCommandParameters(
                        getParameters().getId(),
                        getParameters().getStorageType(),
                        getParameters().isCheckStatus(),
                        getParameters().getLunIds());
        List<LUNs> luns = (List<LUNs>) runVdsCommand(VDSCommandType.GetDeviceList, parameters).getReturnValue();

        // Get LUNs from DB
        List<LUNs> lunsFromDb = lunDao.getAll();
        Map<String, LUNs> lunsFromDbById = lunsFromDb.stream()
                .collect(Collectors.toMap(LUNs::getLUNId, Function.identity()));

        // Get all storage server connections from DB, indexed by iqn+address+port,
        // so that LUN connections returned by VDSM can be enriched with their
        // database IDs. The frontend uses the presence of a DB ID to determine
        // whether a connection is already persisted (and therefore should not
        // offer a disconnect action).
        Map<String, StorageServerConnections> dbConnectionsByKey =
                storageServerConnectionDao.getAll().stream()
                        .collect(Collectors.toMap(
                                GetDeviceListQuery::connectionKey,
                                Function.identity(),
                                (a, b) -> {
                                    log.warn("Multiple storage server connections share transport identity "
                                            + "iqn='{}' address='{}:{}' portal='{}'; using connection id '{}'",
                                            a.getIqn(), a.getConnection(), a.getPort(), a.getPortal(), a.getId());
                                    return a;
                                }));

        // Only connections currently mapped to a LUN are considered persisted.
        // A connection that exists in the DB but is not mapped to any LUN (for
        // example one detached from a domain and kept for re-attachment) must
        // not hide the disconnect action, so it is excluded from enrichment.
        Set<String> mappedConnectionIds = storageServerConnectionLunMapDao.getAll().stream()
                .map(LUNStorageServerConnectionMap::getStorageServerConnection)
                .collect(Collectors.toSet());

        for (LUNs lun : luns) {
            if (lunsFromDbById.containsKey(lun.getLUNId())) {
                LUNs lunFromDb = lunsFromDbById.get(lun.getLUNId());
                lun.setDiskId(lunFromDb.getDiskId());
                lun.setDiskAlias(lunFromDb.getDiskAlias());
                lun.setStorageDomainId(lunFromDb.getStorageDomainId());
                lun.setStorageDomainName(lunFromDb.getStorageDomainName());
            }

            // Enrich LUN connections with their database IDs
            if (lun.getLunConnections() != null) {
                for (StorageServerConnections connection : lun.getLunConnections()) {
                    StorageServerConnections dbConnection =
                            dbConnectionsByKey.get(connectionKey(connection));
                    if (dbConnection != null && mappedConnectionIds.contains(dbConnection.getId())) {
                        connection.setId(dbConnection.getId());
                    }
                }
            }

            returnValue.add(lun);
        }

        getQueryReturnValue().setReturnValue(returnValue);
    }

    private static String connectionKey(StorageServerConnections connection) {
        // Transport identity of an iSCSI session: target (iqn), network portal
        // (connection+port) and target portal group (portal/tpgt). Credentials
        // are deliberately excluded -- they are auth material, not identity
        // (see the matching rationale in SanStorageModelBase.disconnectTarget).
        return Objects.toString(connection.getIqn(), "") + '|' +
                Objects.toString(connection.getConnection(), "") + '|' +
                Objects.toString(connection.getPort(), "") + '|' +
                Objects.toString(connection.getPortal(), "");
    }

    private boolean isActiveVds() {
        VDS vds = vdsDao.get(getParameters().getId());
        if (vds == null) {
            getQueryReturnValue()
                    .setExceptionString(EngineMessage.CANNOT_FETCH_STORAGE_DEVICES_HOST_DOESNT_EXIST_ANYMORE.toString());
        } else if (vds.getStatus() != VDSStatus.Up) {
            getQueryReturnValue().setExceptionString(String.format(
                    "Cannot fetch storage devices, host %s is not UP.",
                    vds.getHostName()));
        } else {
            return true;
        }
        getQueryReturnValue().setSucceeded(false);
        return false;
    }
}
