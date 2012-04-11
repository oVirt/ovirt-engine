package org.ovirt.engine.core.vdsbroker.vdsbroker;

import java.util.HashMap;
import java.util.Map;

import org.ovirt.engine.core.common.businessentities.storage_server_connections;
import org.ovirt.engine.core.common.vdscommands.ConnectStorageServerVDSCommandParameters;
import org.ovirt.engine.core.common.vdscommands.VDSCommandType;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.compat.StringHelper;
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
        Map<String, String>[] result = new HashMap[getParameters().getConnectionList().size()];
        int i = 0;
        for (storage_server_connections connection : getParameters().getConnectionList()) {
            result[i] = CreateStructFromConnection(connection);
            i++;
        }
        return result;
    }

    public static Map<String, String> CreateStructFromConnection(storage_server_connections connection) {
        Map<String, String> con = new HashMap<String, String>();
        con.put("id", (connection.getid() != null) ? connection.getid() : Guid.Empty.toString());
        if (!StringHelper.isNullOrEmpty(connection.getconnection())) {
            con.put("connection", connection.getconnection());
        } else {
            con.put("connection", "");
        }
        if (!StringHelper.isNullOrEmpty(connection.getportal())) {
            con.put("portal", connection.getportal());
        } else {
            con.put("portal", "");
        }
        if (!StringHelper.isNullOrEmpty(connection.getport())) {
            con.put("port", connection.getport());
        } else {
            con.put("port", "");
        }
        if (!StringHelper.isNullOrEmpty(connection.getiqn())) {
            con.put("iqn", connection.getiqn());
        } else {
            con.put("iqn", "");
        }
        if (!StringHelper.isNullOrEmpty(connection.getuser_name())) {
            con.put("user", connection.getuser_name());
        } else {
            con.put("user", "");
        }
        if (!StringHelper.isNullOrEmpty(connection.getpassword())) {
            con.put("password", connection.getpassword());
        } else {
            con.put("password", "");
        }
        // For mnt_options and vfs_type - if they are null or empty
        // we should not send a key with an empty value
        if (!StringHelper.isNullOrEmpty(connection.getMountOptions())) {
            con.put("mnt_options", connection.getMountOptions());
        }
        if (!StringHelper.isNullOrEmpty(connection.getVfsType())) {
            con.put("vfs_type", connection.getVfsType());
        }
        return con;
    }

    @Override
    protected StatusForXmlRpc getReturnStatus() {
        return _result.mStatus;
    }

    protected java.util.HashMap<String, String> GetStatusListFromResult() {
        java.util.HashMap<String, String> result = new java.util.HashMap<String, String>();
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

    private static Log log = LogFactory.getLog(ConnectStorageServerVDSCommand.class);
}
