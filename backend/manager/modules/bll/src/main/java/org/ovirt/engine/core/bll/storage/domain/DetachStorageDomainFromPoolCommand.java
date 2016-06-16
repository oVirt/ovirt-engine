package org.ovirt.engine.core.bll.storage.domain;

import java.util.Collections;
import java.util.Map;

import org.ovirt.engine.core.bll.LockMessagesMatchUtil;
import org.ovirt.engine.core.bll.NonTransactiveCommandAttribute;
import org.ovirt.engine.core.bll.context.CommandContext;
import org.ovirt.engine.core.bll.storage.connection.CINDERStorageHelper;
import org.ovirt.engine.core.common.AuditLogType;
import org.ovirt.engine.core.common.action.DetachStorageDomainFromPoolParameters;
import org.ovirt.engine.core.common.action.LockProperties;
import org.ovirt.engine.core.common.businessentities.StorageDomainStatus;
import org.ovirt.engine.core.common.businessentities.StorageDomainType;
import org.ovirt.engine.core.common.businessentities.StoragePoolIsoMap;
import org.ovirt.engine.core.common.businessentities.StoragePoolIsoMapId;
import org.ovirt.engine.core.common.errors.EngineMessage;
import org.ovirt.engine.core.common.locks.LockingGroup;
import org.ovirt.engine.core.common.utils.Pair;
import org.ovirt.engine.core.common.vdscommands.DetachStorageDomainVDSCommandParameters;
import org.ovirt.engine.core.common.vdscommands.IrsBaseVDSCommandParameters;
import org.ovirt.engine.core.common.vdscommands.VDSCommandType;
import org.ovirt.engine.core.common.vdscommands.VDSReturnValue;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dal.dbbroker.DbFacade;
import org.ovirt.engine.core.utils.transaction.TransactionSupport;

@NonTransactiveCommandAttribute(forceCompensation=true)
public class DetachStorageDomainFromPoolCommand<T extends DetachStorageDomainFromPoolParameters> extends
        StorageDomainCommandBase<T> {

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
                        EngineMessage.ACTION_TYPE_FAILED_OBJECT_LOCKED));
    }

    /**
     * Constructor for command creation when compensation is applied on startup
     */

    public DetachStorageDomainFromPoolCommand(Guid commandId) {
        super(commandId);
    }

    @Override
    protected void executeCommand() {
        if (getStorageDomain().getStorageType().isCinderDomain()) {
            detachCinderStorageDomain();
            return;
        }
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

        log.info(" Detach storage domain: after disconnect storage");
        TransactionSupport.executeInNewTransaction(() -> {
            detachStorageDomainWithEntities(getStorageDomain());
            StoragePoolIsoMap mapToRemove = getStorageDomain().getStoragePoolIsoMapData();
            getCompensationContext().snapshotEntity(mapToRemove);
            DbFacade.getInstance()
                    .getStoragePoolIsoMapDao()
                    .remove(new StoragePoolIsoMapId(mapToRemove.getStorageId(),
                            mapToRemove.getStoragePoolId()));
            // when detaching SD for data center, we should remove any attachment to qos, which is part of the old
            // data center
            DbFacade.getInstance().getDiskProfileDao().nullifyQosForStorageDomain(getStorageDomain().getId());
            getCompensationContext().stateChanged();
            return null;
        });

        log.info(" Detach storage domain: after detach in vds");
        disconnectAllHostsInPool();

        if (returnValue.getSucceeded() && getStorageDomain().getStorageDomainType() == StorageDomainType.ISO) {
            // reset iso for this pool in vdsBroker cache
            runVdsCommand(VDSCommandType.ResetISOPath,
                    new IrsBaseVDSCommandParameters(getParameters().getStoragePoolId()));
        }
        log.info("End detach storage domain");
        setSucceeded(returnValue.getSucceeded());
    }

    private void detachCinderStorageDomain() {
        CINDERStorageHelper CINDERStorageHelper = new CINDERStorageHelper();
        CINDERStorageHelper.detachCinderDomainFromPool(getStorageDomain().getStoragePoolIsoMapData());
        setSucceeded(true);
    }

    @Override
    public AuditLogType getAuditLogTypeValue() {
        return getSucceeded() ? AuditLogType.USER_DETACH_STORAGE_DOMAIN_FROM_POOL
                : AuditLogType.USER_DETACH_STORAGE_DOMAIN_FROM_POOL_FAILED;
    }

    @Override
    protected boolean validate() {
        return canDetachStorageDomainWithVmsAndDisks(getStorageDomain()) &&
                canDetachDomain(getParameters().getDestroyingPool(),
                        getParameters().getRemoveLast(),
                        isInternalExecution());
    }

    @Override
    protected void setActionMessageParameters() {
        addValidationMessage(EngineMessage.VAR__TYPE__STORAGE__DOMAIN);
        addValidationMessage(EngineMessage.VAR__ACTION__DETACH);
    }
}
