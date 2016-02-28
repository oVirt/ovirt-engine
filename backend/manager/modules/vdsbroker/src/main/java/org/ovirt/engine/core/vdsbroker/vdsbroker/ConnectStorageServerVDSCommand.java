package org.ovirt.engine.core.vdsbroker.vdsbroker;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.inject.Inject;

import org.ovirt.engine.core.common.AuditLogType;
import org.ovirt.engine.core.common.businessentities.StorageDomain;
import org.ovirt.engine.core.common.businessentities.StorageServerConnections;
import org.ovirt.engine.core.common.errors.EngineError;
import org.ovirt.engine.core.common.vdscommands.StorageServerConnectionManagementVDSParameters;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dal.dbbroker.DbFacade;
import org.ovirt.engine.core.dal.dbbroker.auditloghandling.AuditLogDirector;
import org.ovirt.engine.core.dal.dbbroker.auditloghandling.AuditLogableBase;
import org.ovirt.engine.core.vdsbroker.storage.StorageConnectionHelper;

public class ConnectStorageServerVDSCommand<P extends StorageServerConnectionManagementVDSParameters>
        extends VdsBrokerCommand<P> {
    protected ServerConnectionStatusReturnForXmlRpc _result;

    @Inject
    private AuditLogDirector auditLogDirector;

    public ConnectStorageServerVDSCommand(P parameters) {
        super(parameters);
    }

    @Override
    protected void executeVdsBrokerCommand() {
        _result = getBroker().connectStorageServer(getParameters().getStorageType().getValue(),
                getParameters().getStoragePoolId().toString(), buildStructFromConnectionListObject());
        proceedProxyReturnValue();
        Map<String, String> returnValue = _result.convertToStatusList();
        setReturnValue(returnValue);
        logFailedStorageConnections(returnValue);
    }

    @Override
    protected void proceedProxyReturnValue() {
        EngineError returnStatus = getReturnValueFromStatus(getReturnStatus());
        switch (returnStatus) {
        case StorageServerConnectionRefIdAlreadyInUse:
        case StorageServerConnectionRefIdDoesNotExist:
            VDSExceptionBase outEx = new VDSErrorException(String.format("Failed in vdscommand %1$s, error = %2$s",
                    getCommandName(), getReturnStatus().message));
            initializeVdsError(returnStatus);
            getVDSReturnValue().setSucceeded(false);
            throw outEx;
        default:
            super.proceedProxyReturnValue();
            break;
        }
    }

    @SuppressWarnings("unchecked")
    protected Map<String, String>[] buildStructFromConnectionListObject() {
        final Map<String, String>[] result = new HashMap[getParameters().getConnectionList().size()];
        int i = 0;
        for (StorageServerConnections connection : getParameters().getConnectionList()) {
            result[i] = StorageConnectionHelper.getInstance().createStructFromConnection(connection,
                    getParameters().getVdsId());
            i++;
        }
        return result;
    }

    private void logFailedStorageConnections(Map<String, String> returnValue) {
        StringBuilder failedDomainNames = new StringBuilder();
        String namesSeparator = ",";
        for (Entry<String, String> result : returnValue.entrySet()) {
            if (!"0".equals(result.getValue())) {
                List<StorageDomain> domains =
                        DbFacade.getInstance().getStorageDomainDao().getAllByConnectionId(new Guid(result.getKey()));
                if (!domains.isEmpty()) {
                    for (StorageDomain domain : domains) {
                        if (failedDomainNames.length() > 0) {
                            failedDomainNames.append(namesSeparator);
                        }
                        failedDomainNames.append(domain.getStorageName());
                    }
                }
            }
        }

        if (failedDomainNames.length() > 0) {
            AuditLogableBase logable = new AuditLogableBase(getParameters().getVdsId());
            logable.addCustomValue("failedStorageDomains", failedDomainNames.toString());
            auditLogDirector.log(logable, AuditLogType.VDS_STORAGES_CONNECTION_FAILED);
        }
    }

    @Override
    protected StatusForXmlRpc getReturnStatus() {
        return _result.getXmlRpcStatus();
    }

    @Override
    protected Object getReturnValueFromBroker() {
        return _result;
    }
}
