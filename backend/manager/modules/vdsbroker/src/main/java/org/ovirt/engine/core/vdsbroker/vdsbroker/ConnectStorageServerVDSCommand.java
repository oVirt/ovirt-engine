package org.ovirt.engine.core.vdsbroker.vdsbroker;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.commons.lang.StringUtils;
import org.ovirt.engine.core.common.AuditLogType;
import org.ovirt.engine.core.common.businessentities.storage_domains;
import org.ovirt.engine.core.common.businessentities.storage_pool;
import org.ovirt.engine.core.common.businessentities.storage_server_connections;
import org.ovirt.engine.core.common.config.Config;
import org.ovirt.engine.core.common.config.ConfigValues;
import org.ovirt.engine.core.common.vdscommands.ConnectStorageServerVDSCommandParameters;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dal.dbbroker.DbFacade;
import org.ovirt.engine.core.dal.dbbroker.auditloghandling.AuditLogDirector;
import org.ovirt.engine.core.dal.dbbroker.auditloghandling.AuditLogableBase;

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
        Map<String, String> returnValue = _result.convertToStatusList();
        setReturnValue(returnValue);
        logFailedStorageConnections(returnValue);
    }

    @SuppressWarnings("unchecked")
    protected Map<String, String>[] BuildStructFromConnectionListObject() {
        final storage_pool storage_pool =
                DbFacade.getInstance().getStoragePoolDao().getForVds(getParameters().getVdsId());
        final Map<String, String>[] result = new HashMap[getParameters().getConnectionList().size()];
        int i = 0;
        for (storage_server_connections connection : getParameters().getConnectionList()) {
            result[i] = CreateStructFromConnection(connection, storage_pool);
            i++;
        }
        return result;
    }

    private static void addOrEmpty(Map<String, String> map, String what, String name) {
        map.put(name, StringUtils.isEmpty(what) ? "" : what);
    }

    private static void addIfNotNullOrEmpty(Map<String, String> map, String what, String name) {
        if (!StringUtils.isEmpty(what)) {
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
        if (storage_pool == null) {
            addIfNotNullOrEmpty(con, connection.getVfsType(), "vfs_type");
        }
        else if (Config.<Boolean> GetValue(ConfigValues.AdvancedNFSOptionsEnabled,
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

    private void logFailedStorageConnections(Map<String, String> returnValue) {
        StringBuilder failedDomainNames = new StringBuilder();
        String namesSeparator = ",";
        for (Entry<String, String> result : returnValue.entrySet()) {
            if (!"0".equals(result.getValue())) {
                List<storage_domains> domains =
                        DbFacade.getInstance().getStorageDomainDao().getAllByConnectionId(new Guid(result.getKey()));
                if (!domains.isEmpty()) {
                    for (storage_domains domain : domains) {
                        if (failedDomainNames.length() > 0) {
                            failedDomainNames.append(namesSeparator);
                        }
                        failedDomainNames.append(domain.getstorage_name());
                    }
                }
            }
        }

        if (failedDomainNames.length() > 0) {
            AuditLogableBase logable = new AuditLogableBase(getParameters().getVdsId());
            logable.AddCustomValue("failedStorageDomains", failedDomainNames.toString());
            AuditLogDirector.log(logable, AuditLogType.VDS_STORAGES_CONNECTION_FAILED);
        }
    }

    @Override
    protected StatusForXmlRpc getReturnStatus() {
        return _result.mStatus;
    }

    @Override
    protected Object getReturnValueFromBroker() {
        return _result;
    }
}
