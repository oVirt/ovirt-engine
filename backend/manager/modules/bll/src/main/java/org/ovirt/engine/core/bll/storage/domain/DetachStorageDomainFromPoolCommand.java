package org.ovirt.engine.core.bll.storage.domain;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import javax.inject.Inject;

import org.ovirt.engine.core.bll.LockMessagesMatchUtil;
import org.ovirt.engine.core.bll.NonTransactiveCommandAttribute;
import org.ovirt.engine.core.bll.context.CommandContext;
import org.ovirt.engine.core.bll.storage.connection.CINDERStorageHelper;
import org.ovirt.engine.core.bll.storage.connection.ManagedBlockStorageHelper;
import org.ovirt.engine.core.common.AuditLogType;
import org.ovirt.engine.core.common.action.DetachStorageDomainFromPoolParameters;
import org.ovirt.engine.core.common.action.LockProperties;
import org.ovirt.engine.core.common.businessentities.StorageDomainStatus;
import org.ovirt.engine.core.common.businessentities.StorageDomainType;
import org.ovirt.engine.core.common.businessentities.StoragePoolIsoMap;
import org.ovirt.engine.core.common.businessentities.StoragePoolIsoMapId;
import org.ovirt.engine.core.common.businessentities.VmStatic;
import org.ovirt.engine.core.common.errors.EngineMessage;
import org.ovirt.engine.core.common.locks.LockingGroup;
import org.ovirt.engine.core.common.utils.Pair;
import org.ovirt.engine.core.common.vdscommands.DetachStorageDomainVDSCommandParameters;
import org.ovirt.engine.core.common.vdscommands.IrsBaseVDSCommandParameters;
import org.ovirt.engine.core.common.vdscommands.VDSCommandType;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dal.dbbroker.auditloghandling.AuditLogDirector;
import org.ovirt.engine.core.dao.StorageDomainDao;
import org.ovirt.engine.core.dao.StoragePoolIsoMapDao;
import org.ovirt.engine.core.dao.VmStaticDao;
import org.ovirt.engine.core.dao.profiles.DiskProfileDao;
import org.ovirt.engine.core.utils.transaction.TransactionSupport;

@NonTransactiveCommandAttribute(forceCompensation=true)
public class DetachStorageDomainFromPoolCommand<T extends DetachStorageDomainFromPoolParameters> extends
        StorageDomainCommandBase<T> {

    @Inject
    private DiskProfileDao diskProfileDao;
    @Inject
    private VmStaticDao vmStaticDao;
    @Inject
    private StoragePoolIsoMapDao storagePoolIsoMapDao;
    @Inject
    private CINDERStorageHelper cinderStorageHelper;
    @Inject
    private ManagedBlockStorageHelper managedBlockStorageHelper;
    @Inject
    private StorageDomainDao storageDomainDao;
    @Inject
    private AuditLogDirector auditLogDirector;

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
        switch (getStorageDomain().getStorageType()) {
            case CINDER:
                detachCinderStorageDomain();
                break;
            case MANAGED_BLOCK_STORAGE:
                detachManagedBlockStorageDomain();
                break;
            default:
                detachStorageDomain();
                break;

        }
    }

    private void detachStorageDomain() {
        log.info("Start detach storage domain");
        changeStorageDomainStatusInTransaction(getStorageDomain().getStoragePoolIsoMapData(),
                StorageDomainStatus.Detaching);
        log.info(" Detach storage domain: before connect");
        connectHostsInUpToDomainStorageServer();
        boolean detachSucceeded = true;
        if (getStorageDomain().getStorageDomainType() == StorageDomainType.Master) {
            log.info(" Detach storage domain(master): Lock master storage domain");
            lockStorageDomain(getStorageDomain());
            log.info(" Detach storage domain(master): destroy storage pool");
            masterDomainDetachWithDestroyPool(getStorageDomain());
            updateDetachedMasterStorageDomain();
            disconnectHostsInUpToDomainStorageServer();
        } else {
            detachSucceeded = detachNonMasterStorageDomain();
        }
        auditOnExistingLeasesIfExist();
        log.info("End detach storage domain");
        setSucceeded(detachSucceeded);
    }

    private void detachManagedBlockStorageDomain() {
        managedBlockStorageHelper.detachManagedStorageDomainFromPool(getStorageDomain().getStoragePoolIsoMapData());
        setSucceeded(true);
    }

    private void updateDetachedMasterStorageDomain() {
        TransactionSupport.executeInNewTransaction(() -> {
            removeStoragePoolIsoMapWithCompensation();
            calcStoragePoolStatusByDomainsStatus();
            return null;
        });
    }

    private boolean detachNonMasterStorageDomain() {
        log.info(" Detach storage domain: after connect");
        boolean detachSucceeded = detachNonMasterStorageDomainFromHost();

        log.info(" Detach storage domain: after disconnect storage");
        TransactionSupport.executeInNewTransaction(() -> {
            releaseStorageDomainMacPool(getVmsOnlyOnStorageDomain());
            detachStorageDomainWithEntities(getStorageDomain());
            removeStoragePoolIsoMapWithCompensation();
            // when detaching SD for data center, we should remove any attachment to qos, which is part of the old
            // data center
            diskProfileDao.nullifyQosForStorageDomain(getStorageDomain().getId());
            return null;
        });

        log.info(" Detach storage domain: after detach in vds");
        disconnectAllHostsInPool();

        if (detachSucceeded && getStorageDomain().getStorageDomainType() == StorageDomainType.ISO) {
            // reset iso for this pool in vdsBroker cache
            runVdsCommand(VDSCommandType.ResetISOPath,
                    new IrsBaseVDSCommandParameters(getParameters().getStoragePoolId()));
        }
        return detachSucceeded;
    }

    private boolean detachNonMasterStorageDomainFromHost() {
        return runVdsCommand(
                VDSCommandType.DetachStorageDomain,
                new DetachStorageDomainVDSCommandParameters(getParameters().getStoragePoolId(),
                        getParameters().getStorageDomainId(), Guid.Empty, getStoragePool()
                        .getMasterDomainVersion())).getSucceeded();
    }

    private void removeStoragePoolIsoMapWithCompensation() {
        StoragePoolIsoMap mapToRemove = getStorageDomain().getStoragePoolIsoMapData();
        getCompensationContext().snapshotEntity(mapToRemove);
        storagePoolIsoMapDao.remove(new StoragePoolIsoMapId(mapToRemove.getStorageId(),
                mapToRemove.getStoragePoolId()));
        getCompensationContext().stateChanged();
    }

    private void detachCinderStorageDomain() {
        cinderStorageHelper.detachCinderDomainFromPool(getStorageDomain().getStoragePoolIsoMapData());
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
                canDetachDomain(getParameters().getDestroyingPool());
    }

    private void auditOnExistingLeasesIfExist() {
        List<VmStatic> entitiesWithLeases = vmStaticDao.getAllWithLeaseOnStorageDomain(getStorageDomain().getId());
        if (!entitiesWithLeases.isEmpty()) {
            String names = entitiesWithLeases.stream().map(VmStatic::getName).collect(Collectors.joining(", "));
            addCustomValue("entitiesNames", names);
            auditLogDirector.log(this, AuditLogType.DETACH_DOMAIN_WITH_VMS_AND_TEMPLATES_LEASES);
        }
    }

    @Override
    protected void setActionMessageParameters() {
        addValidationMessage(EngineMessage.VAR__TYPE__STORAGE__DOMAIN);
        addValidationMessage(EngineMessage.VAR__ACTION__DETACH);
    }
}
