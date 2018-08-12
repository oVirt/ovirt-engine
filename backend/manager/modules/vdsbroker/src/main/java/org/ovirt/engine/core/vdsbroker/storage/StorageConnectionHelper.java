package org.ovirt.engine.core.vdsbroker.storage;

import java.util.Map;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.ovirt.engine.core.common.FeatureSupported;
import org.ovirt.engine.core.common.businessentities.StorageServerConnections;
import org.ovirt.engine.core.common.businessentities.storage.StorageServerConnectionExtension;
import org.ovirt.engine.core.common.utils.Pair;
import org.ovirt.engine.core.common.utils.ValidationUtils;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dao.StorageServerConnectionExtensionDao;
import org.ovirt.engine.core.dao.VdsDao;
import org.ovirt.engine.core.utils.collections.DefaultValueMap;

@Singleton
public class StorageConnectionHelper {
    @Inject
    private StorageServerConnectionExtensionDao storageServerConnectionExtensionDao;

    @Inject
    private VdsDao vdsDao;


    public Map<String, String> createStructFromConnection(final StorageServerConnections connection, final Guid vdsId) {

        Pair<String, String> credentials = getStorageConnectionCredentialsForhost(vdsId, connection);

        // for information, see _connectionDict2ConnectionInfo in vdsm/storage/hsm.py
        DefaultValueMap con = new DefaultValueMap();
        con.put("id", connection.getId(), Guid.Empty.toString());
        con.put("connection", connection.getConnection(), "");
        con.putIfNotEmpty("tpgt", connection.getPortal());
        con.put("port", connection.getPort(), "");
        con.put("iqn", connection.getIqn(), "");
        con.put("user", credentials.getFirst(), "");
        con.put("password", credentials.getSecond(), "");
        if (FeatureSupported.ipv6IscsiSupported(vdsDao.get(vdsId).getClusterCompatibilityVersion())) {
            con.put("ipv6_enabled", isValidIpv6(connection.getConnection()), "false");
        }
        con.putIfNotEmpty("ifaceName", connection.getIface());
        con.putIfNotEmpty("netIfaceName", connection.getNetIfaceName());

        // For mnt_options, vfs_type, and protocol_version - if they are null
        // or empty we should not send a key with an empty value
        con.putIfNotEmpty("mnt_options", connection.getMountOptions());
        con.putIfNotEmpty("vfs_type", connection.getVfsType());
        if (connection.getNfsVersion() != null) {
            con.put("protocol_version", connection.getNfsVersion().getValue());
        }
        con.putIfNotEmpty("timeout", connection.getNfsTimeo());
        con.putIfNotEmpty("retrans", connection.getNfsRetrans());
        return con;
    }

    public Pair<String, String> getStorageConnectionCredentialsForhost(Guid hostId, StorageServerConnections connection) {
        Pair<String, String> credentials;
        StorageServerConnectionExtension connExt = storageServerConnectionExtensionDao.getByHostIdAndTarget(hostId, connection.getIqn());
        if (connExt == null) {
            credentials = new Pair<>(connection.getUserName(), connection.getPassword());
        } else {
            credentials = new Pair<>(connExt.getUserName(), connExt.getPassword());
        }
        return credentials;
    }

    private boolean isValidIpv6(String address) {
        return ValidationUtils.isValidIpv6(address);
    }
}
