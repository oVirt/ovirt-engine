package org.ovirt.engine.core.bll.storage.connection;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.ovirt.engine.core.bll.context.CommandContext;
import org.ovirt.engine.core.common.action.ConnectHostToStoragePoolServersParameters;
import org.ovirt.engine.core.common.action.HostStoragePoolParametersBase;
import org.ovirt.engine.core.common.businessentities.StorageServerConnections;
import org.ovirt.engine.core.common.businessentities.network.VdsNetworkInterface;
import org.ovirt.engine.core.common.businessentities.storage.StorageType;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dao.network.InterfaceDao;

@Singleton
public class ISCSIStorageHelper extends BlockStorageHelperBase {

    @Inject
    private InterfaceDao interfaceDao;

    @Override
    public Collection<StorageType> getTypes() {
        return Collections.singleton(StorageType.ISCSI);
    }

    @Override
    protected StorageType getStorageType() {
        return StorageType.ISCSI;
    }

    @Override
    protected List<StorageServerConnections> prepareConnectionsForConnect(List<StorageServerConnections> connections,
            Guid vdsId) {
        return updateIfaces(connections, vdsId);
    }

    public List<StorageServerConnections> updateIfaces(List<StorageServerConnections> conns, Guid vdsId) {
        List<StorageServerConnections> res = new ArrayList<>(conns);

        for (StorageServerConnections conn : conns) {
            // Get list of endpoints (nics or vlans) that will initiate iscsi sessions.
            // Targets are represented by StorageServerConnections object (connection, iqn, port, portal).
            List<VdsNetworkInterface> ifaces =
                    interfaceDao.getIscsiIfacesByHostIdAndStorageTargetId(vdsId, conn.getId());

            if (!ifaces.isEmpty()) {
                VdsNetworkInterface removedInterface = ifaces.remove(0);
                setInterfaceProperties(conn, removedInterface);

                // Iscsi target is represented by connection object, therefore if this target is approachable
                // from more than one endpoint(initiator) we have to clone this connection per endpoint.
                for (VdsNetworkInterface iface : ifaces) {
                    StorageServerConnections newConn = StorageServerConnections.copyOf(conn);
                    newConn.setId(Guid.newGuid().toString());
                    setInterfaceProperties(newConn, iface);
                    res.add(newConn);
                }
            }
        }

        return res;
    }

    @Override
    public boolean prepareConnectHostToStoragePoolServers(CommandContext cmdContext,
            ConnectHostToStoragePoolServersParameters parameters,
            List<StorageServerConnections> connections) {
        return prepareStorageServer(parameters, connections);
    }

    @Override
    public void prepareDisconnectHostFromStoragePoolServers(HostStoragePoolParametersBase parameters,
            List<StorageServerConnections> connections) {
        prepareStorageServer(parameters, connections);
    }

    private boolean prepareStorageServer(HostStoragePoolParametersBase parameters,
            List<StorageServerConnections> connections) {
        List<StorageServerConnections> res = updateIfaces(connections, parameters.getVds().getId());
        connections.clear();
        connections.addAll(res);
        return true;
    }

    private static void setInterfaceProperties(StorageServerConnections conn, VdsNetworkInterface iface) {
        conn.setIface(iface.getName());
        conn.setNetIfaceName(iface.isBridged() ? iface.getNetworkName() : iface.getName());
    }

    @Override
    protected void fillConnectionDetailsIfNeeded(StorageServerConnections connection) {
        // in case that the connection id is null (in case it wasn't loaded from the db before) - we can attempt to load
        // it from the db by its details.
        if (connection.getId() == null) {
            StorageServerConnections dbConnection = findConnectionWithSameDetails(connection);
            if (dbConnection != null) {
                connection.setId(dbConnection.getId());
            }
        }
    }

    public StorageServerConnections findConnectionWithSameDetails(StorageServerConnections connection) {
        // As we encrypt the password when saving the connection to the DB and each encryption generates different
        // result,
        // we can't query the connections to check if connection with the exact
        // same details was already added - so we query the connections with the same (currently relevant) details and
        // then compare the password after it was already
        // decrypted.
        // NOTE- THIS METHOD IS CURRENTLY USED ALSO FOR FCP connections, change with care.
        List<StorageServerConnections> connections = storageServerConnectionDao.getAllForConnection(connection);
        for (StorageServerConnections dbConnection : connections) {
            if (Objects.equals(dbConnection.getPassword(), connection.getPassword())) {
                return dbConnection;
            }
        }

        return null;
    }

    public List<StorageServerConnections> findConnectionsByAddressPortAndIqn(StorageServerConnections connection) {
        return storageServerConnectionDao.getStorageConnectionsByConnectionPortAndIqn(
                        connection.getConnection(),
                        connection.getPort(),
                        connection.getIqn()
                );
    }
}
