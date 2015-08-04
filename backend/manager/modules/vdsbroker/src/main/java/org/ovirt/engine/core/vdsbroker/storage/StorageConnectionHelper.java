package org.ovirt.engine.core.vdsbroker.storage;

import java.util.Map;

import org.ovirt.engine.core.common.businessentities.StoragePool;
import org.ovirt.engine.core.common.businessentities.StorageServerConnections;
import org.ovirt.engine.core.common.businessentities.storage.StorageServerConnectionExtension;
import org.ovirt.engine.core.common.config.Config;
import org.ovirt.engine.core.common.config.ConfigValues;
import org.ovirt.engine.core.common.utils.Pair;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dal.dbbroker.DbFacade;
import org.ovirt.engine.core.dao.StorageServerConnectionExtensionDao;
import org.ovirt.engine.core.utils.collections.DefaultValueMap;

public class StorageConnectionHelper {

    private static final StorageConnectionHelper INSTANCE = new StorageConnectionHelper();

    private StorageConnectionHelper() {
    }

    public static StorageConnectionHelper getInstance() {
        return INSTANCE;
    }

    public Map<String, String> createStructFromConnection(final StorageServerConnections connection,
            final StoragePool storagePool, final Guid vdsId) {

        Pair<String, String> credentials = getStorageConnectionCredentialsForhost(vdsId, connection);

        // for information, see _connectionDict2ConnectionInfo in vdsm/storage/hsm.py
        DefaultValueMap con = new DefaultValueMap();
        con.put("id", connection.getid(), Guid.Empty.toString());
        con.put("connection", connection.getconnection(), "");
        con.putIfNotEmpty("tpgt", connection.getportal());
        con.put("port", connection.getport(), "");
        con.put("iqn", connection.getiqn(), "");
        con.put("user", credentials.getFirst(), "");
        con.put("password", credentials.getSecond(), "");
        con.putIfNotEmpty("ifaceName", connection.getIface());
        con.putIfNotEmpty("netIfaceName", connection.getNetIfaceName());

        // storage_pool can be null when discovering iscsi send targets or when connecting
        // through vds which has no storage pool
        if (storagePool == null || Config.<Boolean> getValue(ConfigValues.AdvancedNFSOptionsEnabled,
                storagePool.getCompatibilityVersion().getValue())) {
            // For mnt_options, vfs_type, and protocol_version - if they are null
            // or empty we should not send a key with an empty value
            con.putIfNotEmpty("mnt_options", connection.getMountOptions());
            con.putIfNotEmpty("vfs_type", connection.getVfsType());
            if (connection.getNfsVersion() != null) {
                con.put("protocol_version", connection.getNfsVersion().getValue());
            }
            con.putIfNotEmpty("timeout", connection.getNfsTimeo());
            con.putIfNotEmpty("retrans", connection.getNfsRetrans());
        }
        return con;
    }

    public Pair<String, String> getStorageConnectionCredentialsForhost(Guid hostId, StorageServerConnections connection) {
        Pair<String, String> credentials;
        StorageServerConnectionExtension connExt = getConnectionExtensionDao().getByHostIdAndTarget(hostId, connection.getiqn());
        if (connExt == null) {
            credentials = new Pair<>(connection.getuser_name(), connection.getpassword());
        }
        else {
            credentials = new Pair<>(connExt.getUserName(), connExt.getPassword());
        }
        return credentials;
    }

    protected StorageServerConnectionExtensionDao getConnectionExtensionDao() {
        return DbFacade.getInstance().getStorageServerConnectionExtensionDao();
    }
}
