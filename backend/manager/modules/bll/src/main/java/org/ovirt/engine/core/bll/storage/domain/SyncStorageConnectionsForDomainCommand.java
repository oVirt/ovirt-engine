package org.ovirt.engine.core.bll.storage.domain;

import javax.inject.Inject;

import org.ovirt.engine.core.bll.NonTransactiveCommandAttribute;
import org.ovirt.engine.core.bll.context.CommandContext;
import org.ovirt.engine.core.bll.storage.connection.ISCSIStorageHelper;
import org.ovirt.engine.core.bll.validator.HostValidator;
import org.ovirt.engine.core.common.AuditLogType;
import org.ovirt.engine.core.common.action.SyncLunsInfoForBlockStorageDomainParameters;
import org.ovirt.engine.core.common.errors.EngineMessage;
import org.ovirt.engine.core.compat.Guid;

/**
 * Thin, frontend-callable action that triggers the same LUN/connection sync performed
 * internally by {@link SyncLunsInfoForBlockStorageDomainCommand}, for a specific storage
 * domain and host, e.g. from the "Manage Storage Connections" dialog's Save button.
 * <p>
 * Unlike the activation/periodic sync flows, this action persists (creates) any iSCSI
 * connections reported by vdsm that have no matching {@code storage_server_connections} row,
 * so that targets logged into the host ad-hoc become persistent and cluster-visible.
 */
@NonTransactiveCommandAttribute
public class SyncStorageConnectionsForDomainCommand<T extends SyncLunsInfoForBlockStorageDomainParameters>
        extends StorageDomainCommandBase<T> {

    @Inject
    private ISCSIStorageHelper iscsiStorageHelper;

    public SyncStorageConnectionsForDomainCommand(T parameters, CommandContext cmdContext) {
        super(parameters, cmdContext);
        setVdsId(parameters.getVdsId());
        setStorageDomainId(parameters.getStorageDomainId());
    }

    public SyncStorageConnectionsForDomainCommand(Guid commandId) {
        super(commandId);
    }

    @Override
    protected boolean validate() {
        HostValidator hostValidator = getHostValidator();
        if (!validate(hostValidator.hostExists()) || !validate(hostValidator.isUp())) {
            return false;
        }

        if (!checkStorageDomain()) {
            return false;
        }

        if (!getStorageDomain().getStorageType().isBlockDomain()) {
            return failValidation(EngineMessage.ACTION_TYPE_FAILED_STORAGE_DOMAIN_TYPE_ILLEGAL);
        }

        return true;
    }

    protected HostValidator getHostValidator() {
        return HostValidator.createInstance(getVds());
    }

    @Override
    public AuditLogType getAuditLogTypeValue() {
        return getSucceeded() ? AuditLogType.USER_SYNC_STORAGE_CONNECTIONS
                : AuditLogType.USER_SYNC_STORAGE_CONNECTIONS_FAILED;
    }

    @Override
    protected void executeCommand() {
        boolean succeeded;
        try {
            succeeded = iscsiStorageHelper.syncDomainInfo(getStorageDomain(), getVdsId(), true);
        } catch (RuntimeException e) {
            log.warn("Failed to sync storage connections for domain '{}': {}",
                    getStorageDomainId(), e.getMessage());
            log.debug("Exception", e);
            succeeded = false;
        }
        setSucceeded(succeeded);
    }
}
