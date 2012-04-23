package org.ovirt.engine.core.vdsbroker.vdsbroker;

import java.util.HashMap;
import java.util.Map;

import org.ovirt.engine.core.common.businessentities.storage_pool;
import org.ovirt.engine.core.common.businessentities.storage_server_connections;
import org.ovirt.engine.core.common.config.Config;
import org.ovirt.engine.core.common.config.ConfigValues;
import org.ovirt.engine.core.common.vdscommands.ConnectStorageServerVDSCommandParameters;
import org.ovirt.engine.core.common.vdscommands.VDSCommandType;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.compat.StringHelper;
import org.ovirt.engine.core.dal.dbbroker.DbFacade;
import org.ovirt.engine.core.utils.log.Log;
import org.ovirt.engine.core.utils.log.LogFactory;
import org.ovirt.engine.core.vdsbroker.ResourceManager;
import org.ovirt.engine.core.vdsbroker.xmlrpc.XmlRpcStruct;

public class ConnectStorageServerVDSCommand<P extends ConnectStorageServerVDSCommandParameters>
        extends VdsBrokerCommand<P> {
    protected ServerConnectionStatusReturnForXmlRpc _result;

    public ConnectStorageServerVDSCommand(P parameters) {
        super(parameters);
    }

    @Override
    protected void ExecuteVdsBrokerCommand() {
        _result = getBroker().connectStorageServer(getParameters().getStorageType().getValue(),
                getParameters().getStoragePoolId().toString(), BuildStructFromConnectionListObject());
        ProceedProxyReturnValue();
        setReturnValue(GetStatusListFromResult());
    }

    @Override
    public void Rollback() {
        try {
            ResourceManager.getInstance().runVdsCommand(VDSCommandType.DisconnectStorageServer,
                    getParameters());
        } catch (RuntimeException ex) {
            log.error("Exception in Rollback ExecuteVdsBrokerCommand", ex);
        }
    }

    @SuppressWarnings("unchecked")
    protected Map<String, String>[] BuildStructFromConnectionListObject() {
        final storage_pool storage_pool =
                DbFacade.getInstance().getStoragePoolDAO().getForVds(getParameters().getVdsId());
        final Map<String, String>[] result = new HashMap[getParameters().getConnectionList().size()];
        int i = 0;
        for (storage_server_connections connection : getParameters().getConnectionList()) {
            result[i] = CreateStructFromConnection(connection, storage_pool);
            i++;
        }
        return result;
    }

    private static void addOrEmpty(Map<String, String> map, String what, String name) {
        map.put(name, StringHelper.isNullOrEmpty(what) ? "" : what);
    }

    private static void addIfNotNullOrEmpty(Map<String, String> map, String what, String name) {
        if (!StringHelper.isNullOrEmpty(what)) {
            map.put(name, what);
        }
    }

    private static void addIfNotNullOrEmpty(Map<String, String> map, Short what, String name) {
        if (what != null) {
            map.put(name, what.toString());
        }
    }

    public static Map<String, String> CreateStructFromConnection(final storage_server_connections connection,
            final storage_pool storage_pool) {
        // for information, see _connectionDict2ConnectionInfo in vdsm/storage/hsm.py
        Map<String, String> con = new HashMap<String, String>();
        con.put("id", (connection.getid() != null) ? connection.getid() : Guid.Empty.toString());
        addOrEmpty(con, connection.getconnection(), "connection");
        addOrEmpty(con, connection.getportal(), "portal");
        addOrEmpty(con, connection.getport(), "port");
        addOrEmpty(con, connection.getiqn(), "iqn");
        addOrEmpty(con, connection.getuser_name(), "user");
        addOrEmpty(con, connection.getpassword(), "password");
        // storage_pool can be null when discovering iscsi send targets
        if (storage_pool != null
                && Config.<Boolean> GetValue(ConfigValues.AdvancedNFSOptionsEnabled,
                        storage_pool.getcompatibility_version().getValue())) {
            // For mnt_options and vfs_type - if they are null or empty
            // we should not send a key with an empty value
            addIfNotNullOrEmpty(con, connection.getMountOptions(), "mnt_options");
            addIfNotNullOrEmpty(con, connection.getVfsType(), "vfs_type");
            addIfNotNullOrEmpty(con, connection.getNfsVersion(), "protocol_version");
            addIfNotNullOrEmpty(con, connection.getNfsTimeo(), "timeout");
            addIfNotNullOrEmpty(con, connection.getNfsRetrans(), "retrans");
        }
        return con;
    }

    @Override
    protected StatusForXmlRpc getReturnStatus() {
        return _result.mStatus;
    }

    protected Map<String, String> GetStatusListFromResult() {
        HashMap<String, String> result = new HashMap<String, String>();
        for (XmlRpcStruct st : _result.mStatusList) {
            String status = st.getItem("status").toString();
            String id = st.getItem("id").toString();
            result.put(id, status);
        }
        return result;
    }

    @Override
    protected Object getReturnValueFromBroker() {
        return _result;
    }

    private final static Log log = LogFactory.getLog(ConnectStorageServerVDSCommand.class);
}
