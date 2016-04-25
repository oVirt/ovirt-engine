package org.ovirt.engine.core.bll.storage.connection;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.stream.Collectors;

import org.ovirt.engine.core.bll.Backend;
import org.ovirt.engine.core.bll.ValidationResult;
import org.ovirt.engine.core.bll.context.CommandContext;
import org.ovirt.engine.core.bll.provider.storage.OpenStackVolumeProviderProxy;
import org.ovirt.engine.core.common.AuditLogType;
import org.ovirt.engine.core.common.action.ConnectHostToStoragePoolServersParameters;
import org.ovirt.engine.core.common.action.HostStoragePoolParametersBase;
import org.ovirt.engine.core.common.businessentities.NonOperationalReason;
import org.ovirt.engine.core.common.businessentities.Provider;
import org.ovirt.engine.core.common.businessentities.StorageDomain;
import org.ovirt.engine.core.common.businessentities.StorageDomainStatus;
import org.ovirt.engine.core.common.businessentities.StoragePoolIsoMap;
import org.ovirt.engine.core.common.businessentities.StoragePoolIsoMapId;
import org.ovirt.engine.core.common.businessentities.StorageServerConnections;
import org.ovirt.engine.core.common.businessentities.VDS;
import org.ovirt.engine.core.common.businessentities.storage.DiskImage;
import org.ovirt.engine.core.common.businessentities.storage.LibvirtSecret;
import org.ovirt.engine.core.common.businessentities.storage.StorageType;
import org.ovirt.engine.core.common.errors.EngineException;
import org.ovirt.engine.core.common.errors.EngineFault;
import org.ovirt.engine.core.common.errors.EngineMessage;
import org.ovirt.engine.core.common.utils.Pair;
import org.ovirt.engine.core.common.vdscommands.RegisterLibvirtSecretsVDSParameters;
import org.ovirt.engine.core.common.vdscommands.UnregisterLibvirtSecretsVDSParameters;
import org.ovirt.engine.core.common.vdscommands.VDSCommandType;
import org.ovirt.engine.core.common.vdscommands.VDSReturnValue;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dal.dbbroker.DbFacade;
import org.ovirt.engine.core.dal.dbbroker.auditloghandling.AuditLogDirector;
import org.ovirt.engine.core.dal.dbbroker.auditloghandling.AuditLogableBase;
import org.ovirt.engine.core.dao.DiskImageDao;
import org.ovirt.engine.core.dao.LibvirtSecretDao;
import org.ovirt.engine.core.dao.StoragePoolIsoMapDao;
import org.ovirt.engine.core.dao.VdsDao;
import org.ovirt.engine.core.dao.provider.ProviderDao;
import org.ovirt.engine.core.utils.transaction.TransactionSupport;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CINDERStorageHelper extends StorageHelperBase {

    private static Logger log = LoggerFactory.getLogger(CINDERStorageHelper.class);

    private boolean runInNewTransaction = true;

    public boolean isRunInNewTransaction() {
        return runInNewTransaction;
    }

    public void setRunInNewTransaction(boolean runInNewTransaction) {
        this.runInNewTransaction = runInNewTransaction;
    }

    @Override
    protected Pair<Boolean, EngineFault> runConnectionStorageToDomain(StorageDomain storageDomain, Guid vdsId, int type) {
        Provider provider = getProviderDao().get(Guid.createGuidFromString(storageDomain.getStorage()));
        List<LibvirtSecret> libvirtSecrets = getLibvirtSecretDao().getAllByProviderId(provider.getId());
        VDS vds = getVdsDao().get(vdsId);
        if (!isLibrbdAvailable(vds)) {
            log.error("Couldn't found librbd1 package on vds {} (needed for storage domain {}).",
                    vds.getName(), storageDomain.getName());
            addMessageToAuditLog(AuditLogType.NO_LIBRBD_PACKAGE_AVAILABLE_ON_VDS, null, vds.getName());
            return new Pair<>(false, null);
        }
        return registerLibvirtSecrets(storageDomain, vds, libvirtSecrets);
    }

    public static boolean isLibrbdAvailable(VDS vds) {
        return vds.getLibrbdVersion() != null;
    }

    @Override
    public boolean disconnectStorageFromDomainByVdsId(StorageDomain storageDomain, Guid vdsId) {
        Provider provider = getProviderDao().get(Guid.createGuidFromString(storageDomain.getStorage()));
        List<LibvirtSecret> libvirtSecrets = getLibvirtSecretDao().getAllByProviderId(provider.getId());
        VDS vds = getVdsDao().get(vdsId);
        return unregisterLibvirtSecrets(storageDomain, vds, libvirtSecrets);
    }

    @Override
    public boolean prepareConnectHostToStoragePoolServers(CommandContext cmdContext, ConnectHostToStoragePoolServersParameters parameters, List<StorageServerConnections> connections) {
        boolean connectSucceeded = true;
        if (isActiveCinderDomainAvailable(parameters.getStoragePoolId())) {
            // Validate librbd1 package availability
            boolean isLibrbdAvailable = isLibrbdAvailable(parameters.getVds());
            if (!isLibrbdAvailable) {
                log.error("Couldn't found librbd1 package on vds {} (needed for Cinder storage domains).",
                        parameters.getVds().getName());
                setNonOperational(cmdContext, parameters.getVdsId(), NonOperationalReason.LIBRBD_PACKAGE_NOT_AVAILABLE);
            }
            connectSucceeded = isLibrbdAvailable;

            // Register libvirt secrets if needed
            connectSucceeded &= handleLibvirtSecrets(cmdContext, parameters.getVds(), parameters.getStoragePoolId());
        }
        return connectSucceeded;
    }

    /**
     * A utility method for committing a previewed snapshot. The method filters out all the snapshots which will not be
     * part of volume chain once the snapshot get committed, and returns a list of redundant snapshots that should be
     * deleted.
     *
     * @param diskId
     *            - Disk id to fetch all volumes related to it.
     * @param criticalSnapshotsChain
     *            - The snapshot's ids which are critical for the VM since they are used and can not be deleted.
     * @return - A list of redundant snapshots that should be deleted.
     */
    public static List<Guid> getRedundantVolumesToDeleteAfterCommitSnapshot(Guid diskId, Set<Guid> criticalSnapshotsChain) {
        List<Guid> redundantSnapshotIdsToDelete = new ArrayList<>();

        // Fetch all the relevant snapshots to remove.
        List<DiskImage> allVolumesInCinderDisk = getDiskImageDao().getAllSnapshotsForImageGroup(diskId);
        for (DiskImage diskImage : allVolumesInCinderDisk) {
            if (!criticalSnapshotsChain.contains(diskImage.getVmSnapshotId())) {
                redundantSnapshotIdsToDelete.add(diskImage.getVmSnapshotId());
            }
        }
        return redundantSnapshotIdsToDelete;
    }

    public static Pair<Boolean, EngineFault> registerLibvirtSecrets(StorageDomain storageDomain, VDS vds,
                                                           List<LibvirtSecret> libvirtSecrets) {
        VDSReturnValue returnValue;
        if (!libvirtSecrets.isEmpty()) {
            try {
                returnValue = Backend.getInstance().getResourceManager().runVdsCommand(
                        VDSCommandType.RegisterLibvirtSecrets,
                        new RegisterLibvirtSecretsVDSParameters(vds.getId(), libvirtSecrets));
            } catch (RuntimeException e) {
                log.error("Failed to register libvirt secret for storage domain {} on vds {}. Error: {}",
                        storageDomain.getName(), vds.getName(), e.getMessage());
                log.debug("Exception", e);
                return new Pair<>(false, null);
            }
            if (!returnValue.getSucceeded()) {
                addMessageToAuditLog(AuditLogType.FAILED_TO_REGISTER_LIBVIRT_SECRET,
                        storageDomain.getName(), vds.getName());
                log.error("Failed to register libvirt secret for storage domain {} on vds {}.",
                        storageDomain.getName(), vds.getName());
                EngineFault engineFault = new EngineFault();
                engineFault.setError(returnValue.getVdsError().getCode());
                return new Pair<>(false, engineFault);
            }
        }
        return new Pair<>(true, null);
    }

    public static boolean unregisterLibvirtSecrets(
            StorageDomain storageDomain, VDS vds, List<LibvirtSecret> libvirtSecrets) {
        List<Guid> libvirtSecretsUuids = libvirtSecrets.stream().map(LibvirtSecret::getId).collect(Collectors.toList());
        if (!libvirtSecrets.isEmpty()) {
            VDSReturnValue returnValue;
            try {
                returnValue = Backend.getInstance().getResourceManager().runVdsCommand(
                        VDSCommandType.UnregisterLibvirtSecrets,
                        new UnregisterLibvirtSecretsVDSParameters(vds.getId(), libvirtSecretsUuids));
            } catch (RuntimeException e) {
                addMessageToAuditLog(AuditLogType.FAILED_TO_UNREGISTER_LIBVIRT_SECRET,
                        storageDomain.getName(), vds.getName());
                log.error("Failed to unregister libvirt secret for storage domain {} on vds {}. Error: {}",
                        storageDomain.getName(), vds.getName(), e.getMessage());
                log.debug("Exception", e);
                return false;
            }
            if (!returnValue.getSucceeded()) {
                addMessageToAuditLog(AuditLogType.FAILED_TO_UNREGISTER_LIBVIRT_SECRET,
                        storageDomain.getName(), vds.getName());
                log.error("Failed to unregister libvirt secret for storage domain {} on vds {}.",
                        storageDomain.getName(), vds.getName());
                return false;
            }
        }
        return true;
    }

    @Override
    public Pair<Boolean, AuditLogType> disconnectHostFromStoragePoolServersCommandCompleted(HostStoragePoolParametersBase parameters) {
        // unregister all libvirt secrets if needed
        VDSReturnValue returnValue = Backend.getInstance().getResourceManager().runVdsCommand(
                VDSCommandType.RegisterLibvirtSecrets,
                new RegisterLibvirtSecretsVDSParameters(parameters.getVds().getId(), Collections.<LibvirtSecret>emptyList(), true));
        if (!returnValue.getSucceeded()) {
            log.error("Failed to unregister libvirt secret on vds {}.", parameters.getVds().getName());
            return new Pair<>(false, AuditLogType.FAILED_TO_REGISTER_LIBVIRT_SECRET_ON_VDS);
        }
        return new Pair<>(true, null);
    }

    public boolean isActiveCinderDomainAvailable(Guid poolId) {
        return isActiveStorageDomainAvailable(StorageType.CINDER, poolId);
    }

    private boolean handleLibvirtSecrets(CommandContext cmdContext, VDS vds, Guid poolId) {
        List<LibvirtSecret> libvirtSecrets =
                DbFacade.getInstance().getLibvirtSecretDao().getAllByStoragePoolIdFilteredByActiveStorageDomains(poolId);
        if (!libvirtSecrets.isEmpty() && !registerLibvirtSecretsImpl(vds, libvirtSecrets, false)) {
            log.error("Failed to register libvirt secret on vds {}.", vds.getName());
            setNonOperational(cmdContext, vds.getId(), NonOperationalReason.LIBVIRT_SECRETS_REGISTRATION_FAILURE);
            return false;
        }
        return true;
    }

    private boolean registerLibvirtSecretsImpl(VDS vds, List<LibvirtSecret> libvirtSecrets, boolean clearUnusedSecrets) {
        VDSReturnValue returnValue = Backend.getInstance().getResourceManager().runVdsCommand(
                VDSCommandType.RegisterLibvirtSecrets,
                new RegisterLibvirtSecretsVDSParameters(vds.getId(), libvirtSecrets, clearUnusedSecrets));
        return returnValue.getSucceeded();
    }

    private <T> void execute(final Callable<T> callable) {
        if (runInNewTransaction) {
            TransactionSupport.executeInNewTransaction(() -> {
                invokeCallable(callable);
                return null;
            });
        } else {
            invokeCallable(callable);
        }
    }

    private <T> void invokeCallable(Callable<T> callable) {
        try {
            callable.call();
        } catch (Exception e) {
            log.error("Error in CinderStorageHelper.", e);
        }
    }

    public void attachCinderDomainToPool(final Guid storageDomainId, final Guid storagePoolId) {
        execute(() -> {
            StoragePoolIsoMap storagePoolIsoMap =
                    new StoragePoolIsoMap(storageDomainId, storagePoolId, StorageDomainStatus.Maintenance);
            getStoragePoolIsoMapDao().save(storagePoolIsoMap);
            return null;
        });
    }

    public static ValidationResult isCinderHasNoImages(Guid storageDomainId) {
        List<DiskImage> cinderDisks = getDiskImageDao().getAllForStorageDomain(storageDomainId);
        if (!cinderDisks.isEmpty()) {
            return new ValidationResult(EngineMessage.ERROR_CANNOT_DETACH_CINDER_PROVIDER_WITH_IMAGES);
        }
        return ValidationResult.VALID;
    }

    public void activateCinderDomain(Guid storageDomainId, Guid storagePoolId) {
        OpenStackVolumeProviderProxy proxy = OpenStackVolumeProviderProxy.getFromStorageDomainId(storageDomainId);
        if (proxy == null) {
            log.error("Couldn't create an OpenStackVolumeProviderProxy for storage domain ID: {}", storageDomainId);
            return;
        }
        try {
            proxy.testConnection();
            updateCinderDomainStatus(storageDomainId, storagePoolId, StorageDomainStatus.Active);
        } catch (EngineException e) {
            AuditLogableBase loggable = new AuditLogableBase();
            loggable.addCustomValue("CinderException", e.getCause().getCause() != null ?
                    e.getCause().getCause().getMessage() : e.getCause().getMessage());
            new AuditLogDirector().log(loggable, AuditLogType.CINDER_PROVIDER_ERROR);
            throw e;
        }
    }

    public void detachCinderDomainFromPool(final StoragePoolIsoMap mapToRemove) {
        execute(() -> {
            getStoragePoolIsoMapDao().remove(new StoragePoolIsoMapId(mapToRemove.getStorageId(),
                    mapToRemove.getStoragePoolId()));
            return null;
        });
    }

    private void updateCinderDomainStatus(final Guid storageDomainId,
                                          final Guid storagePoolId,
                                          final StorageDomainStatus storageDomainStatus) {
        execute(() -> {
            StoragePoolIsoMap map =
                    getStoragePoolIsoMapDao().get(new StoragePoolIsoMapId(storageDomainId, storagePoolId));
            map.setStatus(storageDomainStatus);
            getStoragePoolIsoMapDao().updateStatus(map.getId(), map.getStatus());
            return null;
        });
    }

    public void deactivateCinderDomain(Guid storageDomainId, Guid storagePoolId) {
        updateCinderDomainStatus(storageDomainId, storagePoolId, StorageDomainStatus.Maintenance);
    }

    private StoragePoolIsoMapDao getStoragePoolIsoMapDao() {
        return getDbFacade().getStoragePoolIsoMapDao();
    }

    private ProviderDao getProviderDao() {
        return getDbFacade().getProviderDao();
    }

    private VdsDao getVdsDao() {
        return getDbFacade().getVdsDao();
    }

    private LibvirtSecretDao getLibvirtSecretDao() {
        return getDbFacade().getLibvirtSecretDao();
    }

    private static DbFacade getDbFacade() {
        return DbFacade.getInstance();
    }

    private static DiskImageDao getDiskImageDao() {
        return getDbFacade().getDiskImageDao();
    }
}
