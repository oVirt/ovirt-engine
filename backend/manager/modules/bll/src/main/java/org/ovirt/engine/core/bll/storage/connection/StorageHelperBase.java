package org.ovirt.engine.core.bll.storage.connection;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;

import org.ovirt.engine.core.bll.context.CommandContext;
import org.ovirt.engine.core.bll.interfaces.BackendInternal;
import org.ovirt.engine.core.bll.job.ExecutionHandler;
import org.ovirt.engine.core.common.AuditLogType;
import org.ovirt.engine.core.common.action.ActionType;
import org.ovirt.engine.core.common.action.ConnectHostToStoragePoolServersParameters;
import org.ovirt.engine.core.common.action.HostStoragePoolParametersBase;
import org.ovirt.engine.core.common.action.SetNonOperationalVdsParameters;
import org.ovirt.engine.core.common.businessentities.NonOperationalReason;
import org.ovirt.engine.core.common.businessentities.StorageDomain;
import org.ovirt.engine.core.common.businessentities.StorageDomainStatic;
import org.ovirt.engine.core.common.businessentities.StorageDomainStatus;
import org.ovirt.engine.core.common.businessentities.StorageServerConnections;
import org.ovirt.engine.core.common.businessentities.VDS;
import org.ovirt.engine.core.common.businessentities.storage.LUNs;
import org.ovirt.engine.core.common.businessentities.storage.StorageType;
import org.ovirt.engine.core.common.errors.EngineError;
import org.ovirt.engine.core.common.errors.EngineFault;
import org.ovirt.engine.core.common.interfaces.VDSBrokerFrontend;
import org.ovirt.engine.core.common.utils.Pair;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dal.dbbroker.auditloghandling.AuditLogDirector;
import org.ovirt.engine.core.dal.dbbroker.auditloghandling.AuditLogable;
import org.ovirt.engine.core.dal.dbbroker.auditloghandling.AuditLogableImpl;
import org.ovirt.engine.core.dao.DiskLunMapDao;
import org.ovirt.engine.core.dao.LunDao;
import org.ovirt.engine.core.dao.StorageDomainDao;
import org.ovirt.engine.core.dao.StorageServerConnectionDao;
import org.slf4j.Logger;

public abstract class StorageHelperBase implements IStorageHelper {
    @Inject
    protected AuditLogDirector auditLogDirector;
    @Inject
    protected BackendInternal backend;
    @Inject
    protected VDSBrokerFrontend resourceManager;
    @Inject
    protected LunDao lunDao;
    @Inject
    protected DiskLunMapDao diskLunMapDao;
    @Inject
    protected StorageServerConnectionDao storageServerConnectionDao;
    @Inject
    private StorageDomainDao storageDomainDao;

    protected abstract Pair<Boolean, EngineFault> runConnectionStorageToDomain(StorageDomain storageDomain, Guid vdsId, int type);

    protected Pair<Boolean, EngineFault> runConnectionStorageToDomain(StorageDomain storageDomain,
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
    public Pair<Boolean, EngineFault> connectStorageToDomainByVdsIdDetails(StorageDomain storageDomain, Guid vdsId) {
        return runConnectionStorageToDomain(storageDomain, vdsId, ActionType.ConnectStorageToVds.getValue());
    }

    @Override
    public boolean disconnectStorageFromDomainByVdsId(StorageDomain storageDomain, Guid vdsId) {
        return runConnectionStorageToDomain(storageDomain, vdsId,
                ActionType.DisconnectStorageServerConnection.getValue()).getFirst();
    }

    @Override
    public boolean connectStorageToLunByVdsId(StorageDomain storageDomain, Guid vdsId, LUNs lun, Guid storagePoolId) {
        return runConnectionStorageToDomain(storageDomain,
                vdsId,
                ActionType.ConnectStorageToVds.getValue(),
                lun,
                storagePoolId).getFirst();
    }

    @Override
    public boolean disconnectStorageFromLunByVdsId(StorageDomain storageDomain, Guid vdsId, LUNs lun) {
        return runConnectionStorageToDomain(storageDomain, vdsId,
                ActionType.DisconnectStorageServerConnection.getValue(), lun, Guid.Empty).getFirst();
    }

    @Override
    public boolean storageDomainRemoved(StorageDomainStatic storageDomain) {
        return true;
    }

    @Override
    public void removeLun(LUNs lun) {
        if (lun.getVolumeGroupId().isEmpty()) {
            lunDao.remove(lun.getLUNId());
            for (StorageServerConnections connection : filterConnectionsUsedByOthers(lun.getLunConnections(),
                    "",
                    lun.getLUNId())) {
                storageServerConnectionDao.remove(connection.getId());
            }
        }
    }

    protected List<StorageServerConnections> filterConnectionsUsedByOthers(
            List<StorageServerConnections> connections, String vgId, final String lunId) {
        return Collections.emptyList();
    }

    @Override
    public boolean isConnectSucceeded(Map<String, String> returnValue,
            List<StorageServerConnections> connections) {
        return true;
    }

    @Override
    public boolean prepareConnectHostToStoragePoolServers(CommandContext cmdContext,
            ConnectHostToStoragePoolServersParameters parameters,
            List<StorageServerConnections> connections) {
        return true;
    }

    @Override
    public void prepareDisconnectHostFromStoragePoolServers(HostStoragePoolParametersBase parameters, List<StorageServerConnections> connections) {
        // default implementation
    }

    @Override
    public Pair<Boolean, AuditLogType> disconnectHostFromStoragePoolServersCommandCompleted(HostStoragePoolParametersBase parameters) {
        return new Pair<>(true, null);
    }

    protected boolean isActiveStorageDomainAvailable(final StorageType storageType, Guid poolId) {
        List<StorageDomain> storageDomains = storageDomainDao.getAllForStoragePool(poolId);
        return storageDomains.stream()
                .anyMatch(s -> s.getStorageType() == storageType && s.getStatus() == StorageDomainStatus.Active);
    }

    protected void setNonOperational(CommandContext cmdContext, Guid vdsId, NonOperationalReason reason) {
        backend.runInternalAction(ActionType.SetNonOperationalVds,
                new SetNonOperationalVdsParameters(vdsId, reason),
                ExecutionHandler.createInternalJobContext(cmdContext));
    }

    protected void removeStorageDomainLuns(StorageDomainStatic storageDomain) {
        final List<LUNs> lunsList = lunDao.getAllForVolumeGroup(storageDomain.getStorage());
        for (LUNs lun : lunsList) {
            removeLunFromStorageDomain(lun);
        }
    }

    private void removeLunFromStorageDomain(LUNs lun) {
        if (diskLunMapDao.getDiskIdByLunId(lun.getLUNId()) == null) {
            lunDao.remove(lun.getLUNId());
        } else {
            lun.setVolumeGroupId("");
            lun.setPhysicalVolumeId(null);
            lunDao.update(lun);
        }
    }


    @Override
    public void removeLunFromStorageDomain(String lunId) {
        removeLunFromStorageDomain(lunDao.get(lunId));
    }

    protected String addToAuditLogErrorMessage(String connection, String errorCode,
            List<StorageServerConnections> connections) {
        return addToAuditLogErrorMessage(connection, errorCode, connections, null);
    }

    protected String addToAuditLogErrorMessage(String connection, String errorCode,
            List<StorageServerConnections> connections, LUNs lun) {
        AuditLogable logable = new AuditLogableImpl();

        String connectionField = getConnectionDescription(connections, connection) +
                (lun == null ? "" : " (LUN " + lun.getLUNId() + ")");
        logable.addCustomValue("Connection", connectionField);

        // Get translated error by error code ,if no translation found (should not happened) ,
        // will set the error code instead.
        String translatedError = getTranslatedStorageError(errorCode);
        logable.addCustomValue("ErrorMessage", translatedError);
        auditLogDirector.log(logable, AuditLogType.STORAGE_DOMAIN_ERROR);
        return connectionField;
    }

    protected void printLog(Logger logger, String connectionField, String errorCode) {
        String translatedError = getTranslatedStorageError(errorCode);
        logger.error(
                "The connection with details '{}' failed because of error code '{}' and error message is: {}",
                connectionField, errorCode, backend.getVdsErrorsTranslator().translateErrorTextSingle(translatedError));
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
        EngineError error = EngineError.forValue(Integer.parseInt(errorCode));
        if (error != null) {
            translatedError = backend.getVdsErrorsTranslator().translateErrorTextSingle(error.toString());
        }
        return translatedError;
    }

    private String getConnectionDescription(List<StorageServerConnections> connections, String connectionId) {
        // Using Guid in order to handle nulls. This can happened when we trying
        // to import an existing domain
        Guid connectionIdGuid = Guid.createGuidFromStringDefaultEmpty(connectionId);
        for (StorageServerConnections connection : connections) {
            Guid connectionGuid = Guid.createGuidFromStringDefaultEmpty(connection.getId());
            if (connectionGuid.equals(connectionIdGuid)) {
                String desc = connection.getConnection();
                if (connection.getIqn() != null) {
                    desc += " " + connection.getIqn();
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

    public void addMessageToAuditLog(AuditLogType auditLogType, StorageDomain storageDomain, VDS vds){
        AuditLogable logable = new AuditLogableImpl();
        logable.setVdsId(vds.getId());
        logable.setVdsName(vds.getName());
        if (storageDomain != null) {
            logable.setStorageDomainId(storageDomain.getId());
            logable.setStorageDomainName(storageDomain.getName());
        }
        auditLogDirector.log(logable, auditLogType);
    }

}
