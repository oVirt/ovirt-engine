package org.ovirt.engine.core.bll.storage;

import java.util.Collections;
import java.util.Map;

import org.ovirt.engine.core.bll.LockMessagesMatchUtil;
import org.ovirt.engine.core.bll.NonTransactiveCommandAttribute;
import org.ovirt.engine.core.bll.context.CommandContext;
import org.ovirt.engine.core.common.AuditLogType;
import org.ovirt.engine.core.common.action.DetachStorageDomainFromPoolParameters;
import org.ovirt.engine.core.common.action.LockProperties;
import org.ovirt.engine.core.common.businessentities.StorageDomainStatus;
import org.ovirt.engine.core.common.businessentities.StorageDomainType;
import org.ovirt.engine.core.common.businessentities.StoragePoolIsoMap;
import org.ovirt.engine.core.common.businessentities.StoragePoolIsoMapId;
import org.ovirt.engine.core.common.errors.VdcBllMessages;
import org.ovirt.engine.core.common.locks.LockingGroup;
import org.ovirt.engine.core.common.utils.Pair;
import org.ovirt.engine.core.common.vdscommands.DetachStorageDomainVDSCommandParameters;
import org.ovirt.engine.core.common.vdscommands.IrsBaseVDSCommandParameters;
import org.ovirt.engine.core.common.vdscommands.VDSCommandType;
import org.ovirt.engine.core.common.vdscommands.VDSReturnValue;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dal.dbbroker.DbFacade;
import org.ovirt.engine.core.utils.transaction.TransactionMethod;
import org.ovirt.engine.core.utils.transaction.TransactionSupport;

@NonTransactiveCommandAttribute(forceCompensation=true)
public class DetachStorageDomainFromPoolCommand<T extends DetachStorageDomainFromPoolParameters> extends
        StorageDomainCommandBase<T> {

    public DetachStorageDomainFromPoolCommand(T parameters) {
        this(parameters, null);
    }

    public DetachStorageDomainFromPoolCommand(T parameters, CommandContext commandContext) {
        super(parameters, commandContext);
    }

    @Override
    protected LockProperties applyLockProperties(LockProperties lockProperties) {
        return lockProperties.withScope(LockProperties.Scope.Command);
    }

    @Override
    protected Map<String, Pair<String, String>> getExclusiveLocks() {
        return Collections.singletonMap(getParameters().getStorageDomainId().toString(),
                LockMessagesMatchUtil.makeLockingPair(LockingGroup.STORAGE,
                        VdcBllMessages.ACTION_TYPE_FAILED_OBJECT_LOCKED));
    }

    /**
     * Constructor for command creation when compensation is applied on startup
     *
     * @param commandId
     */

    protected DetachStorageDomainFromPoolCommand(Guid commandId) {
        super(commandId);
    }

    @Override
    protected void executeCommand() {
        log.info("Start detach storage domain");
        changeStorageDomainStatusInTransaction(getStorageDomain().getStoragePoolIsoMapData(),
                StorageDomainStatus.Detaching);
        log.info(" Detach storage domain: before connect");
        connectHostsInUpToDomainStorageServer();

        log.info(" Detach storage domain: after connect");

        VDSReturnValue returnValue = runVdsCommand(
                VDSCommandType.DetachStorageDomain,
                new DetachStorageDomainVDSCommandParameters(getParameters().getStoragePoolId(),
                        getParameters().getStorageDomainId(), Guid.Empty, getStoragePool()
                                .getMasterDomainVersion()));
        log.info(" Detach storage domain: after detach in vds");
        disconnectAllHostsInPool();

        log.info(" Detach storage domain: after disconnect storage");
        TransactionSupport.executeInNewTransaction(new TransactionMethod<Object>() {
            @Override
            public Object runInTransaction() {
                detachStorageDomainWithEntities(getStorageDomain());
                StoragePoolIsoMap mapToRemove = getStorageDomain().getStoragePoolIsoMapData();
                getCompensationContext().snapshotEntity(mapToRemove);
                DbFacade.getInstance()
                        .getStoragePoolIsoMapDao()
                        .remove(new StoragePoolIsoMapId(mapToRemove.getstorage_id(),
                                mapToRemove.getstorage_pool_id()));
                // when detaching SD for data center, we should remove any attachment to qos, which is part of the old
                // data center
                DbFacade.getInstance().getDiskProfileDao().nullifyQosForStorageDomain(getStorageDomain().getId());
                getCompensationContext().stateChanged();
                return null;
            }
        });
        if (returnValue.getSucceeded() && getStorageDomain().getStorageDomainType() == StorageDomainType.ISO) {
            // reset iso for this pool in vdsBroker cache
            runVdsCommand(VDSCommandType.ResetISOPath,
                    new IrsBaseVDSCommandParameters(getParameters().getStoragePoolId()));
        }
        log.info("End detach storage domain");
        setSucceeded(returnValue.getSucceeded());
    }

    @Override
    public AuditLogType getAuditLogTypeValue() {
        return getSucceeded() ? AuditLogType.USER_DETACH_STORAGE_DOMAIN_FROM_POOL
                : AuditLogType.USER_DETACH_STORAGE_DOMAIN_FROM_POOL_FAILED;
    }

    @Override
    protected boolean canDoAction() {
        return canDetachStorageDomainWithVmsAndDisks(getStorageDomain()) &&
                canDetachDomain(getParameters().getDestroyingPool(),
                        getParameters().getRemoveLast(),
                        isInternalExecution());
    }

    @Override
    protected void setActionMessageParameters() {
        addCanDoActionMessage(VdcBllMessages.VAR__TYPE__STORAGE__DOMAIN);
        addCanDoActionMessage(VdcBllMessages.VAR__ACTION__DETACH);
    }
}
