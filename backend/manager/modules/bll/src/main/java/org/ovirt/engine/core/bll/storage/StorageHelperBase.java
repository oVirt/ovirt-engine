package org.ovirt.engine.core.bll.storage;

import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

import org.ovirt.engine.core.bll.Backend;
import org.ovirt.engine.core.common.AuditLogType;
import org.ovirt.engine.core.common.action.VdcActionType;
import org.ovirt.engine.core.common.businessentities.LUNs;
import org.ovirt.engine.core.common.businessentities.StorageDomainStatic;
import org.ovirt.engine.core.common.businessentities.StorageServerConnections;
import org.ovirt.engine.core.common.businessentities.StorageType;
import org.ovirt.engine.core.common.businessentities.StorageDomain;
import org.ovirt.engine.core.common.errors.VdcBllErrors;
import org.ovirt.engine.core.common.errors.VdcFault;
import org.ovirt.engine.core.common.utils.Pair;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dal.dbbroker.DbFacade;
import org.ovirt.engine.core.dal.dbbroker.auditloghandling.AuditLogDirector;
import org.ovirt.engine.core.dal.dbbroker.auditloghandling.AuditLogableBase;
import org.ovirt.engine.core.dao.LunDAO;
import org.ovirt.engine.core.utils.collections.MultiValueMapUtils;
import org.slf4j.Logger;

public abstract class StorageHelperBase implements IStorageHelper {
    protected abstract Pair<Boolean, VdcFault> runConnectionStorageToDomain(StorageDomain storageDomain, Guid vdsId, int type);

    protected Pair<Boolean, VdcFault> runConnectionStorageToDomain(StorageDomain storageDomain,
            Guid vdsId,
            int type,
            LUNs lun,
            Guid storagePoolId) {
        return new Pair<>(true, null);
    }

    @Override
    public boolean connectStorageToDomainByVdsId(StorageDomain storageDomain, Guid vdsId) {
        return connectStorageToDomainByVdsIdDetails(storageDomain, vdsId).getFirst();
    }

    @Override
    public Pair<Boolean, VdcFault> connectStorageToDomainByVdsIdDetails(StorageDomain storageDomain, Guid vdsId) {
        return runConnectionStorageToDomain(storageDomain, vdsId, VdcActionType.ConnectStorageToVds.getValue());
    }

    @Override
    public boolean disconnectStorageFromDomainByVdsId(StorageDomain storageDomain, Guid vdsId) {
        return runConnectionStorageToDomain(storageDomain, vdsId,
                VdcActionType.DisconnectStorageServerConnection.getValue()).getFirst();
    }

    @Override
    public boolean connectStorageToLunByVdsId(StorageDomain storageDomain, Guid vdsId, LUNs lun, Guid storagePoolId) {
        return runConnectionStorageToDomain(storageDomain,
                vdsId,
                VdcActionType.ConnectStorageToVds.getValue(),
                lun,
                storagePoolId).getFirst();
    }

    @Override
    public boolean disconnectStorageFromLunByVdsId(StorageDomain storageDomain, Guid vdsId, LUNs lun) {
        return runConnectionStorageToDomain(storageDomain, vdsId,
                VdcActionType.DisconnectStorageServerConnection.getValue(), lun, Guid.Empty).getFirst();
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
    public List<StorageServerConnections> getStorageServerConnectionsByDomain(
            StorageDomainStatic storageDomain) {
        return new ArrayList<StorageServerConnections>();
    }

    @Override
    public boolean isConnectSucceeded(Map<String, String> returnValue,
            List<StorageServerConnections> connections) {
        return true;
    }

    public static Map<StorageType, List<StorageServerConnections>> filterConnectionsByStorageType(LUNs lun) {
        Map<StorageType, List<StorageServerConnections>> storageConnectionsForStorageTypeMap =
                new EnumMap<StorageType, List<StorageServerConnections>>(StorageType.class);
        for (StorageServerConnections lunConnections : lun.getLunConnections()) {
            MultiValueMapUtils.addToMap(lunConnections.getstorage_type(),
                    lunConnections,
                    storageConnectionsForStorageTypeMap);
        }
        return storageConnectionsForStorageTypeMap;
    }

    protected static LunDAO getLunDao() {
        return DbFacade.getInstance().getLunDao();
    }

    protected int removeStorageDomainLuns(StorageDomainStatic storageDomain) {
        final List<LUNs> lunsList = getLunDao().getAllForVolumeGroup(storageDomain.getStorage());
        int numOfRemovedLuns = 0;
        for (LUNs lun : lunsList) {
            if (DbFacade.getInstance().getDiskLunMapDao().getDiskIdByLunId(lun.getLUN_id()) == null) {
                getLunDao().remove(lun.getLUN_id());
                numOfRemovedLuns++;
            } else {
                lun.setvolume_group_id("");
                getLunDao().update(lun);
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
        logable.addCustomValue("Connection", connectionField);

        // Get translated error by error code ,if no translation found (should not happened) ,
        // will set the error code instead.
        String translatedError = getTranslatedStorageError(errorCode);
        logable.addCustomValue("ErrorMessage", translatedError);
        AuditLogDirector.log(logable, AuditLogType.STORAGE_DOMAIN_ERROR);
        return connectionField;
    }

    protected void printLog(Logger logger, String connectionField, String errorCode) {
        String translatedError = getTranslatedStorageError(errorCode);
        logger.error(
                "The connection with details '{}' failed because of error code '{}' and error message is: {}",
                connectionField, errorCode, Backend.getInstance().getVdsErrorsTranslator()
                        .TranslateErrorTextSingle(translatedError));
    }

    /**
     * Get translated error by error code ,if no enum for the error code (should not happened) , will set the error code
     * instead. <BR/>
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
        Guid connectionIdGuid = Guid.createGuidFromStringDefaultEmpty(connectionId);
        for (StorageServerConnections connection : connections) {
            Guid connectionGuid = Guid.createGuidFromStringDefaultEmpty(connection.getid());
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

    @Override
    public boolean syncDomainInfo(StorageDomain storageDomain, Guid vdsId) {
        return true;
    }
}
