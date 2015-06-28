package org.ovirt.engine.core.bll.storage;

import java.util.List;
import java.util.Map;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.Predicate;
import org.ovirt.engine.core.bll.InternalCommandAttribute;
import org.ovirt.engine.core.bll.NonTransactiveCommandAttribute;
import org.ovirt.engine.core.bll.context.CommandContext;
import org.ovirt.engine.core.bll.job.ExecutionHandler;
import org.ovirt.engine.core.common.AuditLogType;
import org.ovirt.engine.core.common.FeatureSupported;
import org.ovirt.engine.core.common.action.ConnectHostToStoragePoolServersParameters;
import org.ovirt.engine.core.common.action.SetNonOperationalVdsParameters;
import org.ovirt.engine.core.common.action.VdcActionType;
import org.ovirt.engine.core.common.businessentities.NonOperationalReason;
import org.ovirt.engine.core.common.businessentities.StorageDomain;
import org.ovirt.engine.core.common.businessentities.StorageDomainStatus;
import org.ovirt.engine.core.common.businessentities.StorageServerConnections;
import org.ovirt.engine.core.common.businessentities.storage.LibvirtSecret;
import org.ovirt.engine.core.common.businessentities.storage.StorageType;
import org.ovirt.engine.core.common.vdscommands.StorageServerConnectionManagementVDSParameters;
import org.ovirt.engine.core.common.vdscommands.VDSCommandType;
import org.ovirt.engine.core.dao.LibvirtSecretDao;
import org.ovirt.engine.core.dao.StorageDomainDao;
import org.ovirt.engine.core.dao.VdsDynamicDao;

import javax.inject.Inject;

/**
 * Connect host to all Storage server connections in Storage pool. We
 * considering that connection failed only if data domains failed to connect. If
 * Iso/Export domains failed to connect - only log it.
 */
@NonTransactiveCommandAttribute
@InternalCommandAttribute
public class ConnectHostToStoragePoolServersCommand extends
        ConnectHostToStoragePoolServerCommandBase<ConnectHostToStoragePoolServersParameters> {

    @Inject
    private LibvirtSecretDao libvirtSecretDao;

    @Inject
    private VdsDynamicDao vdsDynamicDao;

    @Inject
    private StorageDomainDao storageDomainDAO;

    public ConnectHostToStoragePoolServersCommand(ConnectHostToStoragePoolServersParameters parameters,
            CommandContext cmdContext) {
        super(parameters, cmdContext);
        setStoragePool(parameters.getStoragePool());
        setVds(parameters.getVds());
    }

    public ConnectHostToStoragePoolServersCommand(ConnectHostToStoragePoolServersParameters parameters) {
        super(parameters);
        setStoragePool(parameters.getStoragePool());
        setVds(parameters.getVds());
    }

    @Override
    protected void executeCommand() {
        initConnectionList(getParameters().isConnectToInactiveDomains());
        setSucceeded(connectStorageServer(getConnectionsTypeMap()));

        if (!getSucceeded()) {
            auditLogDirector.log(this, AuditLogType.CONNECT_STORAGE_SERVERS_FAILED);
        }
    }

    private boolean connectStorageServer(Map<StorageType, List<StorageServerConnections>> connectionsByType) {
        boolean connectSucceeded = true;

        for (Map.Entry<StorageType, List<StorageServerConnections>> connectionToType : connectionsByType.entrySet()) {
            StorageType connectionsType = connectionToType.getKey();
            List<StorageServerConnections> connections = connectionToType.getValue();
            connectSucceeded = connectStorageServersByType(connectionsType, connections) && connectSucceeded;
        }

        if (FeatureSupported.cinderProviderSupported(getStoragePool().getCompatibilityVersion()) &&
                isActiveCinderDomainAvailable()) {
            // Validate librbd1 package availability
            connectSucceeded &= isLibrbdAvailable();

            // Register libvirt secrets if needed
            connectSucceeded &= registerLibvirtSecrets();
        }

        log.info("Host '{}' storage connection was {} ", getVds().getName(), connectSucceeded ? "succeeded" : "failed");

        return connectSucceeded;
    }

    private void setNonOperational(NonOperationalReason reason) {
        runInternalAction(VdcActionType.SetNonOperationalVds,
                new SetNonOperationalVdsParameters(getVds().getId(), reason),
                ExecutionHandler.createInternalJobContext(getContext()));
    }

    private boolean connectStorageServersByType(StorageType storageType, List<StorageServerConnections> connections) {
        if (storageType == StorageType.ISCSI) {
            connections = ISCSIStorageHelper.updateIfaces(connections, getVds().getId());
        }

        Map<String, String> retValues = (Map<String, String>) runVdsCommand(
                        VDSCommandType.ConnectStorageServer,
                        new StorageServerConnectionManagementVDSParameters(getVds().getId(),
                                getStoragePool().getId(), storageType, connections)).getReturnValue();
        return StorageHelperDirector.getInstance().getItem(storageType).isConnectSucceeded(retValues, connections);
    }

    private boolean isActiveCinderDomainAvailable() {
        List<StorageDomain> storageDomains = storageDomainDAO.getAllForStoragePool(getStoragePoolId());
        return CollectionUtils.exists(storageDomains, new Predicate() {
            @Override
            public boolean evaluate(Object o) {
                StorageDomain storageDomain = (StorageDomain) o;
                return storageDomain.getStorageType().isCinderDomain() &&
                        storageDomain.getStatus() == StorageDomainStatus.Active;
            }
        });
    }

    private boolean isLibrbdAvailable() {
        if (!CINDERStorageHelper.isLibrbdAvailable(getVds())) {
            log.error("Couldn't found librbd1 package on vds {} (needed for Cinder storage domains).",
                    getVds().getName());
            setNonOperational(NonOperationalReason.LIBRBD_PACKAGE_NOT_AVAILABLE);
            return false;
        }
        return true;
    }

    private boolean registerLibvirtSecrets() {
        List<LibvirtSecret> libvirtSecrets =
                libvirtSecretDao.getAllByStoragePoolIdFilteredByActiveStorageDomains(getStoragePoolId());
        if (!libvirtSecrets.isEmpty()) {
            return registerLibvirtSecrets(libvirtSecrets, false);
        }
        return true;
    }
}
