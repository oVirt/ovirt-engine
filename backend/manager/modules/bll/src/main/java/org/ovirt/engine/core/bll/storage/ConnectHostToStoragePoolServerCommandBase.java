package org.ovirt.engine.core.bll.storage;

import java.util.Collections;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.ovirt.engine.core.bll.InternalCommandAttribute;
import org.ovirt.engine.core.bll.context.CommandContext;
import org.ovirt.engine.core.bll.storage.connection.FCPStorageHelper;
import org.ovirt.engine.core.common.AuditLogType;
import org.ovirt.engine.core.common.FeatureSupported;
import org.ovirt.engine.core.common.action.StoragePoolParametersBase;
import org.ovirt.engine.core.common.businessentities.StorageDomain;
import org.ovirt.engine.core.common.businessentities.StorageDomainStatus;
import org.ovirt.engine.core.common.businessentities.StorageServerConnections;
import org.ovirt.engine.core.common.businessentities.storage.LibvirtSecret;
import org.ovirt.engine.core.common.businessentities.storage.StorageType;
import org.ovirt.engine.core.common.vdscommands.RegisterLibvirtSecretsVDSParameters;
import org.ovirt.engine.core.common.vdscommands.VDSCommandType;
import org.ovirt.engine.core.common.vdscommands.VDSReturnValue;
import org.ovirt.engine.core.dal.dbbroker.DbFacade;
import org.ovirt.engine.core.dal.dbbroker.auditloghandling.AuditLogableBase;
import org.ovirt.engine.core.utils.collections.MultiValueMapUtils;

@InternalCommandAttribute
public abstract class ConnectHostToStoragePoolServerCommandBase<T extends StoragePoolParametersBase> extends
        StorageHandlingCommandBase<T> {
    private List<StorageServerConnections> _connections;
    private Map<StorageType, List<StorageServerConnections>> connectionsTypeMap;

    public ConnectHostToStoragePoolServerCommandBase(T parameters, CommandContext cmdContext) {
        super(parameters, cmdContext);
    }

    public ConnectHostToStoragePoolServerCommandBase(T parameters) {
        super(parameters);
    }

    protected Map<StorageType, List<StorageServerConnections>> getConnectionsTypeMap() {
        return connectionsTypeMap;
    }

    protected void initConnectionList(boolean includeInactiveDomains) {
        Set<StorageDomainStatus> statuses;

        statuses = includeInactiveDomains ?
                EnumSet.of(StorageDomainStatus.Active, StorageDomainStatus.Unknown, StorageDomainStatus.Inactive) :
                EnumSet.of(StorageDomainStatus.Active, StorageDomainStatus.Unknown);

        _connections =
                DbFacade.getInstance()
                        .getStorageServerConnectionDao()
                        .getStorageConnectionsByStorageTypeAndStatus(getStoragePool().getId(),
                                null,
                                statuses);
        updateConnectionsTypeMap();
        updateConnectionMapForFiberChannel(statuses);
    }

    private void updateConnectionsTypeMap() {
        connectionsTypeMap = new HashMap<>();
        for (StorageServerConnections conn : _connections) {
            StorageType connType = conn.getstorage_type();
            MultiValueMapUtils.addToMap(connType, conn, connectionsTypeMap);
        }
    }

    private void updateConnectionMapForFiberChannel(Set<StorageDomainStatus> statuses) {
        if (FeatureSupported.refreshLunSupported(getStoragePool().getCompatibilityVersion())) {
            List<StorageDomain> storageDomainList =
                    getStorageDomainDao().getAllForStoragePool(getStoragePool().getId());
            for (StorageDomain sd : storageDomainList) {
                if (sd.getStorageType() == StorageType.FCP && statuses.contains(sd.getStatus())) {
                    MultiValueMapUtils.addToMap(StorageType.FCP,
                            FCPStorageHelper.getFCPConnection(),
                            getConnectionsTypeMap());
                    break;
                }
            }
        }
    }

    protected boolean registerLibvirtSecrets(List<LibvirtSecret> libvirtSecrets, boolean clearUnusedSecrets) {
        VDSReturnValue returnValue = runVdsCommand(
                VDSCommandType.RegisterLibvirtSecrets,
                new RegisterLibvirtSecretsVDSParameters(getVdsId(), libvirtSecrets, clearUnusedSecrets));
        if (!returnValue.getSucceeded()) {
            auditLogDirector.log(new AuditLogableBase(getVdsId()),
                    AuditLogType.FAILED_TO_REGISTER_LIBVIRT_SECRET_ON_VDS);
            log.error("Failed to register libvirt secret on vds {}.", getVds().getName());
            return false;
        }
        return true;
    }

    protected boolean unregisterLibvirtSecrets() {
        if (FeatureSupported.cinderProviderSupported(getStoragePool().getCompatibilityVersion())) {
            // Unregister all libvirt secrets if needed
            return registerLibvirtSecrets(Collections.<LibvirtSecret>emptyList(), true);
        }
        return true;
    }
}
