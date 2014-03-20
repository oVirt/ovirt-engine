package org.ovirt.engine.core.bll.storage;

import java.util.Collections;
import java.util.Map;

import org.ovirt.engine.core.bll.LockMessagesMatchUtil;
import org.ovirt.engine.core.bll.NonTransactiveCommandAttribute;
import org.ovirt.engine.core.bll.context.CommandContext;
import org.ovirt.engine.core.common.AuditLogType;
import org.ovirt.engine.core.common.action.LockProperties;
import org.ovirt.engine.core.common.action.LockProperties.Scope;
import org.ovirt.engine.core.common.action.DetachStorageDomainFromPoolParameters;
import org.ovirt.engine.core.common.action.RemoveStorageDomainParameters;
import org.ovirt.engine.core.common.action.VdcActionType;
import org.ovirt.engine.core.common.businessentities.StorageDomain;
import org.ovirt.engine.core.common.businessentities.StorageDomainType;
import org.ovirt.engine.core.common.businessentities.StoragePoolIsoMapId;
import org.ovirt.engine.core.common.businessentities.StorageType;
import org.ovirt.engine.core.common.businessentities.VDS;
import org.ovirt.engine.core.common.errors.VdcBLLException;
import org.ovirt.engine.core.common.errors.VdcBllErrors;
import org.ovirt.engine.core.common.errors.VdcBllMessages;
import org.ovirt.engine.core.common.locks.LockingGroup;
import org.ovirt.engine.core.common.utils.Pair;
import org.ovirt.engine.core.common.vdscommands.FormatStorageDomainVDSCommandParameters;
import org.ovirt.engine.core.common.vdscommands.VDSCommandType;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.utils.transaction.TransactionMethod;
import org.ovirt.engine.core.utils.transaction.TransactionSupport;

@NonTransactiveCommandAttribute
public class RemoveStorageDomainCommand<T extends RemoveStorageDomainParameters> extends StorageDomainCommandBase<T> {
    public RemoveStorageDomainCommand(T parameters) {
        this(parameters, null);
    }

    public RemoveStorageDomainCommand(T parameters, CommandContext commandContext) {
        super(parameters, commandContext);
    }

    @Override
    protected LockProperties applyLockProperties(LockProperties lockProperties) {
        return lockProperties.withScope(Scope.Execution);
    }

    @Override
    protected void executeCommand() {
        final StorageDomain dom = getStorageDomain();
        VDS vds = getVds();
        boolean format = getParameters().getDoFormat();

        setSucceeded(false);

        if (isLocalFs(dom) && isDomainAttached(dom) && !detachStorage(dom)) {
            return;
        }

        if (!isISO(dom) && !isExport(dom) || format) {
            if (!connectStorage()) {
                return;
            }

            boolean failed = !formatStorage(dom, vds);

            disconnectStorage();

            if (failed) {
                return;
            }
        }

        TransactionSupport.executeInNewTransaction(new TransactionMethod<Object>() {
            @Override
            public Object runInTransaction() {
                getStorageHelper(dom).storageDomainRemoved(dom.getStorageStaticData());
                getStorageDomainDAO().remove(dom.getId());
                return null;
            }
        });

        setSucceeded(true);
    }

    @Override
    public AuditLogType getAuditLogTypeValue() {
        return getSucceeded() ? AuditLogType.USER_REMOVE_STORAGE_DOMAIN
                : AuditLogType.USER_REMOVE_STORAGE_DOMAIN_FAILED;
    }

    @Override
    protected void setActionMessageParameters() {
        super.setActionMessageParameters();
        addCanDoActionMessage(VdcBllMessages.VAR__ACTION__REMOVE);
    }

    @Override
    protected boolean canDoAction() {
        if (!super.canDoAction()) {
            return false;
        }

        StorageDomain dom = getStorageDomain();
        if (dom == null) {
            addCanDoActionMessage(VdcBllMessages.ACTION_TYPE_FAILED_STORAGE_DOMAIN_NOT_EXIST);
            return false;
        }

        VDS vds = getVds();
        boolean format = getParameters().getDoFormat();
        boolean localFs = isLocalFs(dom);

        if (!checkStorageDomain() || !checkStorageDomainSharedStatusNotLocked(dom)) {
            return false;
        }

        if (!localFs && !checkStorageDomainNotInPool()) {
            return false;
        }

        if (localFs && isDomainAttached(dom) && !canDetachDomain(getParameters().getDestroyingPool(), false, true)) {
            return false;
        }

        if (vds == null) {
            if (localFs) {
                if (!initializeVds()) {
                    return false;
                }
            } else {
                addCanDoActionMessage(VdcBllMessages.CANNOT_REMOVE_STORAGE_DOMAIN_INVALID_HOST_ID);
                return false;
            }
        }

        if (isDataDomain(dom) && !format) {
            addCanDoActionMessage(VdcBllMessages.ERROR_CANNOT_REMOVE_STORAGE_DOMAIN_DO_FORMAT);
            return false;
        }

        if (dom.getStorageType() == StorageType.GLANCE) {
            addCanDoActionMessage(VdcBllMessages.ERROR_CANNOT_MANAGE_STORAGE_DOMAIN);
            return false;
        }

        return true;
    }

    private boolean connectStorage() {
        return getStorageHelper(getStorageDomain()).connectStorageToDomainByVdsId(getStorageDomain(),
                getVds().getId());
    }

    private void disconnectStorage() {
        getStorageHelper(getStorageDomain()).disconnectStorageFromDomainByVdsId(getStorageDomain(),
                getVds().getId());
    }

    protected boolean isLocalFs(StorageDomain dom) {
        return dom.getStorageType() == StorageType.LOCALFS;
    }

    protected boolean isDataDomain(StorageDomain dom) {
        return dom.getStorageDomainType() == StorageDomainType.Data;
    }

    protected boolean isISO(StorageDomain dom) {
        return dom.getStorageDomainType() == StorageDomainType.ISO;
    }

    protected boolean isExport(StorageDomain dom) {
        return dom.getStorageDomainType() == StorageDomainType.ImportExport;
    }

    protected boolean isDomainAttached(StorageDomain storageDomain) {
        if (storageDomain.getStoragePoolId() == null) {
            return false;
        }

        Guid storageDomainId = storageDomain.getId();
        Guid storagePoolId = storageDomain.getStoragePoolId();

        return getDbFacade().getStoragePoolIsoMapDao()
                .get(new StoragePoolIsoMapId(storageDomainId, storagePoolId)) != null;
    }

    protected boolean detachStorage(StorageDomain dom) {
        Guid domId = dom.getId();
        Guid poolId = dom.getStoragePoolId();
        DetachStorageDomainFromPoolParameters params = new DetachStorageDomainFromPoolParameters(domId, poolId);
        params.setDestroyingPool(getParameters().getDestroyingPool());

        return getBackend()
                .runInternalAction(VdcActionType.DetachStorageDomainFromPool,
                        params).getSucceeded();
    }

    protected boolean formatStorage(StorageDomain dom, VDS vds) {
        try {
            return runVdsCommand(VDSCommandType.FormatStorageDomain,
                            new FormatStorageDomainVDSCommandParameters(vds.getId(), dom.getId())).getSucceeded();
        } catch (VdcBLLException e) {
            if (e.getErrorCode() != VdcBllErrors.StorageDomainDoesNotExist) {
                throw e;
            }
            log.warnFormat("Storage Domain {0} which was about to be formatted does not exist in VDS {1}",
                    dom.getName(),
                    vds.getName());
            return true;
        }
    }

    @Override
    protected Map<String, Pair<String, String>> getExclusiveLocks() {
        return Collections.singletonMap(getParameters().getStorageDomainId().toString(),
                LockMessagesMatchUtil.makeLockingPair(LockingGroup.STORAGE, VdcBllMessages.ACTION_TYPE_FAILED_OBJECT_LOCKED));
    }
}
