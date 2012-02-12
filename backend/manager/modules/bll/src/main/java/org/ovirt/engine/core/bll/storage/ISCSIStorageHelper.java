package org.ovirt.engine.core.bll.storage;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

import org.apache.commons.collections.CollectionUtils;

import org.ovirt.engine.core.bll.Backend;
import org.ovirt.engine.core.common.businessentities.LUN_storage_server_connection_map;
import org.ovirt.engine.core.common.businessentities.LUNs;
import org.ovirt.engine.core.common.businessentities.StorageType;
import org.ovirt.engine.core.common.businessentities.VDS;
import org.ovirt.engine.core.common.businessentities.storage_domain_static;
import org.ovirt.engine.core.common.businessentities.storage_domains;
import org.ovirt.engine.core.common.businessentities.storage_server_connections;
import org.ovirt.engine.core.common.vdscommands.ConnectStorageServerVDSCommandParameters;
import org.ovirt.engine.core.common.vdscommands.VDSCommandType;
import org.ovirt.engine.core.common.vdscommands.VDSReturnValue;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.utils.log.Log;
import org.ovirt.engine.core.utils.log.LogFactory;
import org.ovirt.engine.core.dal.dbbroker.DbFacade;
import org.ovirt.engine.core.utils.linq.Function;
import org.ovirt.engine.core.utils.linq.LinqUtils;
import org.ovirt.engine.core.utils.linq.Predicate;

public class ISCSIStorageHelper extends StorageHelperBase {

    protected static final Log log = LogFactory.getLog(ISCSIStorageHelper.class);

    @Override
    protected boolean RunConnectionStorageToDomain(storage_domains storageDomain, Guid vdsId, int type) {
        return RunConnectionStorageToDomain(storageDomain, vdsId, type, null);
    }

    @Override
    protected boolean RunConnectionStorageToDomain(storage_domains storageDomain, Guid vdsId, int type, LUNs lun) {
        boolean isSuccess = true;
        List<storage_server_connections> list =
                (lun == null) ? DbFacade.getInstance()
                        .getStorageServerConnectionDAO().getAllForVolumeGroup(storageDomain.getstorage())
                        : lun.getLunConnections();

        if (list.size() != 0) {
            if (VDSCommandType.forValue(type) == VDSCommandType.DisconnectStorageServer) {
                list = FilterConnectionsUsedByOthers(list, storageDomain.getstorage(), lun != null ? lun.getLUN_id()
                        : "");
            }
            VDSReturnValue returnValue = Backend
                    .getInstance()
                    .getResourceManager()
                    .RunVdsCommand(
                            VDSCommandType.forValue(type),
                            new ConnectStorageServerVDSCommandParameters(vdsId,
                                    ((storageDomain.getstorage_pool_id()) != null) ? storageDomain.getstorage_pool_id()
                                            .getValue() : Guid.Empty, StorageType.ISCSI, list));
            isSuccess = returnValue.getSucceeded();
            if (isSuccess && VDSCommandType.forValue(type) == VDSCommandType.ConnectStorageServer) {
                isSuccess = IsConnectSucceeded((HashMap<String, String>) returnValue.getReturnValue(), list);
            }
        }
        return isSuccess;
    }

    private List<storage_server_connections> FilterConnectionsUsedByOthers(
            List<storage_server_connections> connections, String vgId) {
        return FilterConnectionsUsedByOthers(connections, vgId, "");
    }

    private List<storage_server_connections> FilterConnectionsUsedByOthers(
            List<storage_server_connections> connections, String vgId, final String lunId) {
        // if we have lun id then filter by this lun
        // else get vg's luns from db
        // Iterable<String> lunsByVg = null; //LINQ lunId != string.Empty ?
        // LINQ DbFacade.Instance.GetLunsByVGId(vgId).Select(a => a.LUN_id) :
        // LINQ new List<string> { lunId };
        List<String> lunsByVg =
                lunId.isEmpty() ? LinqUtils.foreach(DbFacade.getInstance().getLunDAO().getAllForVolumeGroup(vgId),
                        new Function<LUNs, String>() {
                            @Override
                            public String eval(LUNs a) {
                                return a.getLUN_id();
                            }
                        }) : new LinkedList<String>() {
                    {
                        add(lunId);
                    }
                };

        java.util.ArrayList<storage_server_connections> toRemove =
                new java.util.ArrayList<storage_server_connections>();
        for (storage_server_connections connection : connections) {
            // if (true) //LINQ 31899
            // DbFacade.Instance.GetLunsByStorageServerConnection(connection.id).
            // Select(a => a.LUN_id).Except(lunsByVg).Count() > 0)

            List<String> list = LinqUtils.foreach(
                    DbFacade.getInstance().getLunDAO().getAllForStorageServerConnection(connection.getid()),
                    new Function<LUNs, String>() {
                        @Override
                        public String eval(LUNs a) {
                            return a.getLUN_id();
                        }
                    });

            if (0 < CollectionUtils.subtract(list, lunsByVg).size()) {
                toRemove.add(connection);
            }
        }
        // return null; //LINQ 31899 connections.Except(toRemove).ToList();
        return new ArrayList<storage_server_connections>(CollectionUtils.subtract(connections, toRemove));
    }

    @Override
    public boolean ValidateStoragePoolConnectionsInHost(VDS vds, List<storage_server_connections> connections,
            Guid storagePoolId) {
        if (connections.size() > 0) {
            java.util.HashMap<String, String> validateConnections = (java.util.HashMap<String, String>) Backend
                    .getInstance()
                    .getResourceManager()
                    .RunVdsCommand(
                            VDSCommandType.ValidateStorageServerConnection,
                            new ConnectStorageServerVDSCommandParameters(vds.getvds_id(), storagePoolId,
                                    StorageType.ISCSI, connections)).getReturnValue();

            return IsConnectSucceeded(validateConnections, connections);
        }
        return true;
    }

    @Override
    public boolean IsConnectSucceeded(final java.util.HashMap<String, String> returnValue,
            List<storage_server_connections> connections) {
        // java.util.ArrayList<String> failedConnectionsList = null; // LINQ
        // returnValue.Where(a => a.Value == false).
        boolean result = true;
        List<String> failedConnectionsList = LinqUtils.filter(returnValue.keySet(), new Predicate<String>() {
            @Override
            public boolean eval(String a) {
                return !"0".equals(returnValue.get(a));
            }
        });
        // LINQ Select(a => a.Key).ToList();
        for (String failedConnection : failedConnectionsList) {
            List<LUNs> failedLuns = DbFacade.getInstance().getLunDAO()
                    .getAllForStorageServerConnection(failedConnection);
            if (!failedLuns.isEmpty()) {
                for (LUNs lun : failedLuns) {
                    /**
                     * TODO: Vitaly check if luns in the same pool.
                     */
                    List<String> strings =
                            LinqUtils.foreach(
                                    DbFacade.getInstance()
                                            .getStorageServerConnectionLunMapDAO()
                                            .getAll(lun.getLUN_id()),
                                    new Function<LUN_storage_server_connection_map, String>() {
                                        @Override
                                        public String eval(LUN_storage_server_connection_map a) {
                                            return a.getstorage_server_connection();
                                        }
                                    });
                    if (CollectionUtils.subtract(strings, failedConnectionsList).size() == 0) {
                        // At case of failure the appropriate log message will be
                        // added
                        log.infoFormat("The lun with id {0} was reported as problematic !", lun.getphisical_volume_id());
                        for (String connectionFailed : failedConnectionsList) {
                            String connectionField =
                                    addToAuditLogErrorMessage(connectionFailed,
                                            returnValue.get(connectionFailed),
                                            connections);
                            printLog(log, connectionField, returnValue.get(connectionFailed));
                        }
                        return false;
                    }
                }
            } else {
                result = false;
                printLog(log, failedConnection, returnValue.get(failedConnection));
            }
        }
        return result;
    }

    @Override
    public boolean StorageDomainRemoved(storage_domain_static storageDomain) {
        List<storage_server_connections> list = DbFacade.getInstance()
                .getStorageServerConnectionDAO().getAllForVolumeGroup(storageDomain.getstorage());
        List<LUNs> lunsList = DbFacade.getInstance().getLunDAO().getAllForVolumeGroup(storageDomain.getstorage());
        if (lunsList.size() != 0) {
            for (LUNs lun : lunsList) {
                DbFacade.getInstance().getLunDAO().remove(lun.getLUN_id());
            }
        }
        if (list.size() != 0) {
            list = FilterConnectionsUsedByOthers(list, storageDomain.getstorage());
            for (storage_server_connections connection : list) {
                DbFacade.getInstance().getStorageServerConnectionDAO().remove(connection.getid());
            }
        }
        return true;
    }

    @Override
    public boolean ConnectStorageToDomainByStoragePoolId(storage_domains storageDomain, Guid storagePoolId) {
        return RunForSingleConnectionInHost(storageDomain, storagePoolId,
                VDSCommandType.ConnectStorageServer.getValue());
    }

    @Override
    public boolean DisconnectStorageFromDomainByStoragePoolId(storage_domains storageDomain, Guid storagePoolId) {
        return RunForSingleConnectionInHost(storageDomain, storagePoolId,
                VDSCommandType.DisconnectStorageServer.getValue());
    }

    @Override
    public boolean ConnectStorageToDomainByVdsId(storage_domains storageDomain, Guid vdsId) {
        return RunConnectionStorageToDomain(storageDomain, vdsId, VDSCommandType.ConnectStorageServer.getValue());
    }

    @Override
    public boolean DisconnectStorageFromDomainByVdsId(storage_domains storageDomain, Guid vdsId) {
        return RunConnectionStorageToDomain(storageDomain, vdsId, VDSCommandType.DisconnectStorageServer.getValue());
    }

    @Override
    public boolean ConnectStorageToLunByVdsId(storage_domains storageDomain, Guid vdsId, LUNs lun) {
        return RunConnectionStorageToDomain(storageDomain, vdsId, VDSCommandType.ConnectStorageServer.getValue(), lun);
    }

    @Override
    public boolean DisconnectStorageFromLunByVdsId(storage_domains storageDomain, Guid vdsId, LUNs lun) {
        return RunConnectionStorageToDomain(storageDomain, vdsId, VDSCommandType.DisconnectStorageServer.getValue(),
                lun);
    }

    @Override
    public List<storage_server_connections> GetStorageServerConnectionsByDomain(
            storage_domain_static storageDomain) {
        return DbFacade.getInstance().getStorageServerConnectionDAO().getAllForVolumeGroup(storageDomain.getstorage());
    }
}
