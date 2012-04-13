package org.ovirt.engine.core.bll.storage;

import java.util.List;
import java.util.Map;

import org.ovirt.engine.core.bll.Backend;
import org.ovirt.engine.core.common.AuditLogType;
import org.ovirt.engine.core.common.action.StorageServerConnectionParametersBase;
import org.ovirt.engine.core.common.action.VdcActionType;
import org.ovirt.engine.core.common.businessentities.LUNs;
import org.ovirt.engine.core.common.businessentities.StoragePoolStatus;
import org.ovirt.engine.core.common.businessentities.VDS;
import org.ovirt.engine.core.common.businessentities.storage_domain_static;
import org.ovirt.engine.core.common.businessentities.storage_domains;
import org.ovirt.engine.core.common.businessentities.storage_pool;
import org.ovirt.engine.core.common.businessentities.storage_server_connections;
import org.ovirt.engine.core.common.errors.VdcBllErrors;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dal.dbbroker.DbFacade;
import org.ovirt.engine.core.dal.dbbroker.auditloghandling.AuditLogDirector;
import org.ovirt.engine.core.dal.dbbroker.auditloghandling.AuditLogableBase;
import org.ovirt.engine.core.utils.log.Log;

public abstract class StorageHelperBase implements IStorageHelper {
    @Override
    public boolean ConnectStorageToDomainByStoragePoolId(storage_domains storageDomain, Guid storagePoolId) {
        return RunForSingleConnectionInHost(storageDomain, storagePoolId, VdcActionType.ConnectStorageToVds.getValue());
    }

    public boolean RunForSingleConnectionInHost(storage_domains storageDomain, Guid storagePoolId, int type) {
        boolean returnValue = false;
        storage_pool pool = DbFacade.getInstance().getStoragePoolDAO().get(storagePoolId);
        Guid vdsId = pool.getspm_vds_id() != null ? pool.getspm_vds_id().getValue() : Guid.Empty;

        if (!vdsId.equals(Guid.Empty)) {
            returnValue = RunConnectionStorageToDomain(storageDomain, vdsId, type);
        }
        return returnValue;
    }

    @Override
    public boolean DisconnectStorageFromDomainByStoragePoolId(storage_domains storageDomain, Guid storagePoolId) {
        return RunForSingleConnectionInHost(storageDomain, storagePoolId,
                VdcActionType.RemoveStorageServerConnection.getValue());
    }

    protected void RunForAllConnectionsInPool(VdcActionType type, VDS vds) {
        storage_pool pool = DbFacade.getInstance().getStoragePoolDAO().get(vds.getstorage_pool_id());
        if (pool.getstatus() != StoragePoolStatus.Uninitialized) {
            List<storage_server_connections> connections = DbFacade.getInstance()
                    .getStorageServerConnectionDAO().getAllForStoragePool(vds.getstorage_pool_id());
            for (storage_server_connections connection : connections) {
                Backend.getInstance().runInternalAction(type,
                        new StorageServerConnectionParametersBase(connection, vds.getId()));
            }
        }
    }

    protected abstract boolean RunConnectionStorageToDomain(storage_domains storageDomain, Guid vdsId, int type);

    protected boolean RunConnectionStorageToDomain(storage_domains storageDomain, Guid vdsId, int type, LUNs lun) {
        return true;
    }

    @Override
    public boolean ConnectStorageToDomainByVdsId(storage_domains storageDomain, Guid vdsId) {
        return RunConnectionStorageToDomain(storageDomain, vdsId, VdcActionType.ConnectStorageToVds.getValue());
    }

    @Override
    public boolean DisconnectStorageFromDomainByVdsId(storage_domains storageDomain, Guid vdsId) {
        return RunConnectionStorageToDomain(storageDomain, vdsId,
                VdcActionType.RemoveStorageServerConnection.getValue());
    }

    @Override
    public boolean ConnectStorageToLunByVdsId(storage_domains storageDomain, Guid vdsId, LUNs lun) {
        return RunConnectionStorageToDomain(storageDomain, vdsId, VdcActionType.ConnectStorageToVds.getValue(), lun);
    }

    @Override
    public boolean DisconnectStorageFromLunByVdsId(storage_domains storageDomain, Guid vdsId, LUNs lun) {
        return RunConnectionStorageToDomain(storageDomain, vdsId,
                VdcActionType.RemoveStorageServerConnection.getValue(), lun);
    }

    @Override
    public boolean StorageDomainRemoved(storage_domain_static storageDomain) {
        return true;
    }

    @Override
    public boolean ValidateStoragePoolConnectionsInHost(VDS vds, List<storage_server_connections> connections,
            Guid storagePoolId) {
        return true;
    }

    @Override
    public List<storage_server_connections> GetStorageServerConnectionsByDomain(
            storage_domain_static storageDomain) {
        return new java.util.ArrayList<storage_server_connections>();
    }

    @Override
    public boolean IsConnectSucceeded(Map<String, String> returnValue,
            List<storage_server_connections> connections) {
        return true;
    }

    protected String addToAuditLogErrorMessage(String connection, String errorCode,
            List<storage_server_connections> connections) {
        String connectionField = getConnectionField(connections, connection);
        AuditLogableBase logable = new AuditLogableBase();
        logable.AddCustomValue("Connection", connectionField);

        // Get translated error by error code ,if no translation found (should not happened) ,
        // will set the error code instead.
        String translatedError = getTranslatedStorageError(errorCode);
        logable.AddCustomValue("ErrorCode", translatedError);
        AuditLogDirector.log(logable, AuditLogType.STORAGE_DOMAIN_ERROR);
        return connectionField;
    }

    protected void printLog(Log logger, String connectionField, String errorCode) {
        String translatedError = getTranslatedStorageError(errorCode);
        logger.errorFormat(
                 "The connection with details {0} failed because of error code {1} and error message is: {2}",
                  connectionField, errorCode, Backend.getInstance().getVdsErrorsTranslator()
                                    .TranslateErrorTextSingle(translatedError));
    }

    /**
     * Get translated error by error code ,if no enum for the error code (should not happened) ,
     * will set the error code instead.
     * When no enum found for the error code, we should check it with the vdsm team.
     *
     * @param errorCode
     *            - The error code we want to translate.
     * @return - Translated error if found or error code.
     */
    private String getTranslatedStorageError(String errorCode) {
        String translatedError = errorCode;
        VdcBllErrors error = VdcBllErrors.forValue(Integer.parseInt(errorCode));
        if (error != null) {
            translatedError =
                    Backend.getInstance()
                            .getVdsErrorsTranslator()
                            .TranslateErrorTextSingle(error.toString());
        }
        return translatedError;
    }

    private String getConnectionField(List<storage_server_connections> connections, String connectionId) {
        // Using Guid in order to handle nulls. This can happened when we trying
        // to import an existing domain
        Guid connectionIdGuid = Guid.createGuidFromString(connectionId);
        for (storage_server_connections connection : connections) {
            Guid connectionGuid = Guid.createGuidFromString(connection.getid());
            if (connectionGuid.equals(connectionIdGuid)) {
                return connection.getconnection();
            }
        }
        return "";
    }
}
