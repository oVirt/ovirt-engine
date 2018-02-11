package org.ovirt.engine.core.bll.storage.connection;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.ovirt.engine.core.bll.context.CommandContext;
import org.ovirt.engine.core.bll.provider.ProviderProxyFactory;
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
import org.ovirt.engine.core.common.businessentities.storage.LibvirtSecret;
import org.ovirt.engine.core.common.businessentities.storage.StorageType;
import org.ovirt.engine.core.common.errors.EngineException;
import org.ovirt.engine.core.common.errors.EngineFault;
import org.ovirt.engine.core.common.utils.Pair;
import org.ovirt.engine.core.common.vdscommands.RegisterLibvirtSecretsVDSParameters;
import org.ovirt.engine.core.common.vdscommands.UnregisterLibvirtSecretsVDSParameters;
import org.ovirt.engine.core.common.vdscommands.VDSCommandType;
import org.ovirt.engine.core.common.vdscommands.VDSReturnValue;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dal.dbbroker.auditloghandling.AuditLogable;
import org.ovirt.engine.core.dal.dbbroker.auditloghandling.AuditLogableImpl;
import org.ovirt.engine.core.dao.LibvirtSecretDao;
import org.ovirt.engine.core.dao.StoragePoolIsoMapDao;
import org.ovirt.engine.core.dao.VdsDao;
import org.ovirt.engine.core.dao.provider.ProviderDao;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Singleton
public class CINDERStorageHelper extends StorageHelperBase {

    private static Logger log = LoggerFactory.getLogger(CINDERStorageHelper.class);

    @Inject
    private ProviderProxyFactory providerProxyFactory;
    @Inject
    private ProviderDao providerDao;
    @Inject
    private LibvirtSecretDao libvirtSecretDao;
    @Inject
    private VdsDao vdsDao;
    @Inject
    private StoragePoolIsoMapDao storagePoolIsoMapDao;

    @Override
    public Collection<StorageType> getTypes() {
        return Collections.singleton(StorageType.CINDER);
    }

    @Override
    protected Pair<Boolean, EngineFault> runConnectionStorageToDomain(StorageDomain storageDomain, Guid vdsId, int type) {
        Provider<?> provider = providerDao.get(Guid.createGuidFromString(storageDomain.getStorage()));
        List<LibvirtSecret> libvirtSecrets = libvirtSecretDao.getAllByProviderId(provider.getId());
        VDS vds = vdsDao.get(vdsId);
        if (!isLibrbdAvailable(vds)) {
            log.error("Couldn't found librbd1 package on vds {} (needed for storage domain {}).",
                    vds.getName(), storageDomain.getName());
            addMessageToAuditLog(AuditLogType.NO_LIBRBD_PACKAGE_AVAILABLE_ON_VDS, null, vds);
            return new Pair<>(false, null);
        }
        return registerLibvirtSecrets(storageDomain, vds, libvirtSecrets);
    }

    private static boolean isLibrbdAvailable(VDS vds) {
        return vds.getLibrbdVersion() != null;
    }

    @Override
    public boolean disconnectStorageFromDomainByVdsId(StorageDomain storageDomain, Guid vdsId) {
        Provider<?> provider = providerDao.get(Guid.createGuidFromString(storageDomain.getStorage()));
        List<LibvirtSecret> libvirtSecrets = libvirtSecretDao.getAllByProviderId(provider.getId());
        VDS vds = vdsDao.get(vdsId);
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

    public Pair<Boolean, EngineFault> registerLibvirtSecrets
            (StorageDomain storageDomain, VDS vds, List<LibvirtSecret> libvirtSecrets) {
        VDSReturnValue returnValue;
        if (!libvirtSecrets.isEmpty()) {
            try {
                returnValue = resourceManager.runVdsCommand(
                        VDSCommandType.RegisterLibvirtSecrets,
                        new RegisterLibvirtSecretsVDSParameters(vds.getId(), libvirtSecrets));
            } catch (RuntimeException e) {
                log.error("Failed to register libvirt secret for storage domain {} on vds {}. Error: {}",
                        storageDomain.getName(), vds.getName(), e.getMessage());
                log.debug("Exception", e);
                return new Pair<>(false, null);
            }
            if (!returnValue.getSucceeded()) {
                addMessageToAuditLog(AuditLogType.FAILED_TO_REGISTER_LIBVIRT_SECRET, storageDomain, vds);
                log.error("Failed to register libvirt secret for storage domain {} on vds {}.",
                        storageDomain.getName(), vds.getName());
                EngineFault engineFault = new EngineFault();
                engineFault.setError(returnValue.getVdsError().getCode());
                return new Pair<>(false, engineFault);
            }
        }
        return new Pair<>(true, null);
    }

    public boolean unregisterLibvirtSecrets(StorageDomain storageDomain, VDS vds, List<LibvirtSecret> libvirtSecrets) {
        List<Guid> libvirtSecretsUuids = libvirtSecrets.stream().map(LibvirtSecret::getId).collect(Collectors.toList());
        if (!libvirtSecrets.isEmpty()) {
            VDSReturnValue returnValue;
            try {
                returnValue = resourceManager.runVdsCommand(
                        VDSCommandType.UnregisterLibvirtSecrets,
                        new UnregisterLibvirtSecretsVDSParameters(vds.getId(), libvirtSecretsUuids));
            } catch (RuntimeException e) {
                addMessageToAuditLog(AuditLogType.FAILED_TO_UNREGISTER_LIBVIRT_SECRET, storageDomain, vds);
                log.error("Failed to unregister libvirt secret for storage domain {} on vds {}. Error: {}",
                        storageDomain.getName(), vds.getName(), e.getMessage());
                log.debug("Exception", e);
                return false;
            }
            if (!returnValue.getSucceeded()) {
                addMessageToAuditLog(AuditLogType.FAILED_TO_UNREGISTER_LIBVIRT_SECRET, storageDomain, vds);
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
        VDSReturnValue returnValue = resourceManager.runVdsCommand(
                VDSCommandType.RegisterLibvirtSecrets,
                new RegisterLibvirtSecretsVDSParameters(parameters.getVds().getId(), Collections.emptyList(), true));
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
                libvirtSecretDao.getAllByStoragePoolIdFilteredByActiveStorageDomains(poolId);
        if (!libvirtSecrets.isEmpty() && !registerLibvirtSecretsImpl(vds, libvirtSecrets)) {
            log.error("Failed to register libvirt secret on vds {}.", vds.getName());
            setNonOperational(cmdContext, vds.getId(), NonOperationalReason.LIBVIRT_SECRETS_REGISTRATION_FAILURE);
            return false;
        }
        return true;
    }

    private boolean registerLibvirtSecretsImpl(VDS vds, List<LibvirtSecret> libvirtSecrets) {
        VDSReturnValue returnValue = resourceManager.runVdsCommand(
                VDSCommandType.RegisterLibvirtSecrets,
                new RegisterLibvirtSecretsVDSParameters(vds.getId(), libvirtSecrets, false));
        return returnValue.getSucceeded();
    }

    public void attachCinderDomainToPool(final Guid storageDomainId, final Guid storagePoolId) {
        StoragePoolIsoMap storagePoolIsoMap =
                new StoragePoolIsoMap(storageDomainId, storagePoolId, StorageDomainStatus.Maintenance);
        storagePoolIsoMapDao.save(storagePoolIsoMap);
    }

    public void activateCinderDomain(Guid storageDomainId, Guid storagePoolId) {
        OpenStackVolumeProviderProxy proxy =
                OpenStackVolumeProviderProxy.getFromStorageDomainId(storageDomainId, providerProxyFactory);
        if (proxy == null) {
            log.error("Couldn't create an OpenStackVolumeProviderProxy for storage domain ID: {}", storageDomainId);
            return;
        }
        try {
            proxy.testConnection();
            updateCinderDomainStatus(storageDomainId, storagePoolId, StorageDomainStatus.Active);
        } catch (EngineException e) {
            AuditLogable loggable = new AuditLogableImpl();
            loggable.addCustomValue("CinderException", e.getCause().getCause() != null ?
                    e.getCause().getCause().getMessage() : e.getCause().getMessage());
            auditLogDirector.log(loggable, AuditLogType.CINDER_PROVIDER_ERROR);
            throw e;
        }
    }

    public void detachCinderDomainFromPool(final StoragePoolIsoMap mapToRemove) {
        storagePoolIsoMapDao.remove(new StoragePoolIsoMapId(mapToRemove.getStorageId(), mapToRemove.getStoragePoolId()));
    }

    private void updateCinderDomainStatus(final Guid storageDomainId,
                                          final Guid storagePoolId,
                                          final StorageDomainStatus storageDomainStatus) {
        StoragePoolIsoMap map = storagePoolIsoMapDao.get(new StoragePoolIsoMapId(storageDomainId, storagePoolId));
        map.setStatus(storageDomainStatus);
        storagePoolIsoMapDao.updateStatus(map.getId(), map.getStatus());
    }

    public void deactivateCinderDomain(Guid storageDomainId, Guid storagePoolId) {
        updateCinderDomainStatus(storageDomainId, storagePoolId, StorageDomainStatus.Maintenance);
    }
}
