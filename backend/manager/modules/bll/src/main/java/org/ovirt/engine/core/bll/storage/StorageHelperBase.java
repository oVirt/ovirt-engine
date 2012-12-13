package org.ovirt.engine.core.bll.storage;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.ovirt.engine.core.bll.Backend;
import org.ovirt.engine.core.common.AuditLogType;
import org.ovirt.engine.core.common.action.VdcActionType;
import org.ovirt.engine.core.common.businessentities.LUNs;
import org.ovirt.engine.core.common.businessentities.VDS;
import org.ovirt.engine.core.common.businessentities.StorageDomainStatic;
import org.ovirt.engine.core.common.businessentities.storage_domains;
import org.ovirt.engine.core.common.businessentities.storage_pool;
import org.ovirt.engine.core.common.businessentities.StorageServerConnections;
import org.ovirt.engine.core.common.errors.VdcBllErrors;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dal.dbbroker.DbFacade;
import org.ovirt.engine.core.dal.dbbroker.auditloghandling.AuditLogDirector;
import org.ovirt.engine.core.dal.dbbroker.auditloghandling.AuditLogableBase;
import org.ovirt.engine.core.dao.LunDAO;
import org.ovirt.engine.core.utils.log.Log;

public abstract class StorageHelperBase implements IStorageHelper {
    @Override
    public boolean connectStorageToDomainByStoragePoolId(storage_domains storageDomain, Guid storagePoolId) {
        return runForSingleConnectionInHost(storageDomain, storagePoolId, VdcActionType.ConnectStorageToVds.getValue());
    }

    public boolean runForSingleConnectionInHost(storage_domains storageDomain, Guid storagePoolId, int type) {
        boolean returnValue = false;
        storage_pool pool = DbFacade.getInstance().getStoragePoolDao().get(storagePoolId);
        Guid vdsId = pool.getspm_vds_id() != null ? pool.getspm_vds_id().getValue() : Guid.Empty;

        if (!vdsId.equals(Guid.Empty)) {
            returnValue = runConnectionStorageToDomain(storageDomain, vdsId, type);
        }
        return returnValue;
    }

    @Override
    public boolean disconnectStorageFromDomainByStoragePoolId(storage_domains storageDomain, Guid storagePoolId) {
        return runForSingleConnectionInHost(storageDomain, storagePoolId,
                VdcActionType.RemoveStorageServerConnection.getValue());
    }

    protected abstract boolean runConnectionStorageToDomain(storage_domains storageDomain, Guid vdsId, int type);

    protected boolean runConnectionStorageToDomain(storage_domains storageDomain, Guid vdsId, int type, LUNs lun, Guid storagePoolId) {
        return true;
    }

    @Override
    public boolean connectStorageToDomainByVdsId(storage_domains storageDomain, Guid vdsId) {
        return runConnectionStorageToDomain(storageDomain, vdsId, VdcActionType.ConnectStorageToVds.getValue());
    }

    @Override
    public boolean disconnectStorageFromDomainByVdsId(storage_domains storageDomain, Guid vdsId) {
        return runConnectionStorageToDomain(storageDomain, vdsId,
                VdcActionType.RemoveStorageServerConnection.getValue());
    }

    @Override
    public boolean connectStorageToLunByVdsId(storage_domains storageDomain, Guid vdsId, LUNs lun, Guid storagePoolId) {
        return runConnectionStorageToDomain(storageDomain, vdsId, VdcActionType.ConnectStorageToVds.getValue(), lun, storagePoolId);
    }

    @Override
    public boolean disconnectStorageFromLunByVdsId(storage_domains storageDomain, Guid vdsId, LUNs lun) {
        return runConnectionStorageToDomain(storageDomain, vdsId,
                VdcActionType.RemoveStorageServerConnection.getValue(), lun, Guid.Empty);
    }

    @Override
    public boolean storageDomainRemoved(StorageDomainStatic storageDomain) {
        return true;
    }

    @Override
    public void removeLun(LUNs lun) {
        if (lun.getvolume_group_id().isEmpty()) {
            DbFacade.getInstance().getLunDao().remove(lun.getLUN_id());
            for (StorageServerConnections connection : filterConnectionsUsedByOthers(lun.getLunConnections(),
                    "",
                    lun.getLUN_id())) {
                DbFacade.getInstance().getStorageServerConnectionDao().remove(connection.getid());
            }
        }
    }

    protected List<StorageServerConnections> filterConnectionsUsedByOthers(
            List<StorageServerConnections> connections, String vgId, final String lunId) {
        return Collections.emptyList();
    }

    @Override
    public boolean validateStoragePoolConnectionsInHost(VDS vds, List<StorageServerConnections> connections,
            Guid storagePoolId) {
        return true;
    }

    @Override
    public List<StorageServerConnections> GetStorageServerConnectionsByDomain(
            StorageDomainStatic storageDomain) {
        return new java.util.ArrayList<StorageServerConnections>();
    }

    @Override
    public boolean IsConnectSucceeded(Map<String, String> returnValue,
            List<StorageServerConnections> connections) {
        return true;
    }

    protected static LunDAO getLunDao() {
        return DbFacade.getInstance().getLunDao();
    }

    protected int removeStorageDomainLuns(StorageDomainStatic storageDomain) {
        final List<LUNs> lunsList = getLunDao().getAllForVolumeGroup(storageDomain.getstorage());
        int numOfRemovedLuns = 0;
        for (LUNs lun : lunsList) {
            if (DbFacade.getInstance().getDiskLunMapDao().getDiskIdByLunId(lun.getLUN_id()) == null) {
                getLunDao().remove(lun.getLUN_id());
                numOfRemovedLuns++;
            } else {
                getLunDao().updateLUNsVolumeGroupId(lun.getLUN_id(), "");
            }
        }
        return numOfRemovedLuns;
    }

    protected String addToAuditLogErrorMessage(String connection, String errorCode,
            List<StorageServerConnections> connections) {
        return addToAuditLogErrorMessage(connection, errorCode, connections, null);
    }

    protected String addToAuditLogErrorMessage(String connection, String errorCode,
            List<StorageServerConnections> connections, LUNs lun) {
        AuditLogableBase logable = new AuditLogableBase();

        String connectionField = getConnectionDescription(connections, connection) +
                (lun == null ? "" : " (LUN " + lun.getLUN_id() + ")");
        logable.AddCustomValue("Connection", connectionField);

        // Get translated error by error code ,if no translation found (should not happened) ,
        // will set the error code instead.
        String translatedError = getTranslatedStorageError(errorCode);
        logable.AddCustomValue("ErrorMessage", translatedError);
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

    private String getConnectionDescription(List<StorageServerConnections> connections, String connectionId) {
        // Using Guid in order to handle nulls. This can happened when we trying
        // to import an existing domain
        Guid connectionIdGuid = Guid.createGuidFromString(connectionId);
        for (StorageServerConnections connection : connections) {
            Guid connectionGuid = Guid.createGuidFromString(connection.getid());
            if (connectionGuid.equals(connectionIdGuid)) {
                String desc = connection.getconnection();
                if (connection.getiqn() != null) {
                    desc += " " + connection.getiqn();
                }
                return desc;
            }
        }
        return "";
    }
}
