package org.ovirt.engine.core.bll.storage;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.ovirt.engine.core.bll.LockMessagesMatchUtil;
import org.ovirt.engine.core.bll.NonTransactiveCommandAttribute;
import org.ovirt.engine.core.bll.context.CommandContext;
import org.ovirt.engine.core.bll.validator.storage.StorageDomainToPoolRelationValidator;
import org.ovirt.engine.core.bll.validator.storage.StorageDomainValidator;
import org.ovirt.engine.core.common.AuditLogType;
import org.ovirt.engine.core.common.action.DetachStorageDomainFromPoolParameters;
import org.ovirt.engine.core.common.action.LockProperties;
import org.ovirt.engine.core.common.action.LockProperties.Scope;
import org.ovirt.engine.core.common.action.RemoveStorageDomainParameters;
import org.ovirt.engine.core.common.action.VdcActionType;
import org.ovirt.engine.core.common.businessentities.StorageDomain;
import org.ovirt.engine.core.common.businessentities.StoragePoolIsoMapId;
import org.ovirt.engine.core.common.businessentities.StorageType;
import org.ovirt.engine.core.common.businessentities.VDS;
import org.ovirt.engine.core.common.errors.VdcBLLException;
import org.ovirt.engine.core.common.errors.VdcBllErrors;
import org.ovirt.engine.core.common.errors.VdcBllMessages;
import org.ovirt.engine.core.common.errors.VdcFault;
import org.ovirt.engine.core.common.locks.LockingGroup;
import org.ovirt.engine.core.common.queries.StorageDomainsAndStoragePoolIdQueryParameters;
import org.ovirt.engine.core.common.queries.VdcQueryType;
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

        if (format) {
            Pair<Boolean, VdcFault> connectResult = connectStorage();
            if (!connectResult.getFirst()) {
                getReturnValue().setFault(connectResult.getSecond());
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
            return failCanDoAction(VdcBllMessages.ACTION_TYPE_FAILED_STORAGE_DOMAIN_NOT_EXIST);
        }

        if (getParameters().getDoFormat() && isStorageDomainAttached(dom)) {
            return failCanDoAction(VdcBllMessages.ACTION_TYPE_FAILED_FORMAT_STORAGE_DOMAIN_WITH_ATTACHED_DATA_DOMAIN);
        }

        VDS vds = getVds();
        boolean localFs = isLocalFs(dom);
        StorageDomainToPoolRelationValidator domainPoolValidator = createDomainToPoolValidator(dom);
        StorageDomainValidator domainValidator = new StorageDomainValidator(dom);

        if (!checkStorageDomain() || !validate(domainValidator.checkStorageDomainSharedStatusNotLocked())) {
            return false;
        }

        if (!localFs && !validate(domainPoolValidator.isStorageDomainNotInAnyPool())) {
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
                return failCanDoAction(VdcBllMessages.CANNOT_REMOVE_STORAGE_DOMAIN_INVALID_HOST_ID);
            }
        }

        if (dom.getStorageType() == StorageType.GLANCE) {
            return failCanDoAction(VdcBllMessages.ERROR_CANNOT_MANAGE_STORAGE_DOMAIN);
        }

        return true;
    }

    protected boolean isStorageDomainAttached(StorageDomain dom) {
        List<StorageDomain> storageDomainList =
                (List<StorageDomain>) getBackend().runInternalQuery(VdcQueryType.GetStorageDomainsWithAttachedStoragePoolGuid,
                        new StorageDomainsAndStoragePoolIdQueryParameters(dom, getStoragePoolId(), getVds().getId(), false))
                        .getReturnValue();
        return !storageDomainList.isEmpty();
    }

    protected StorageDomainToPoolRelationValidator createDomainToPoolValidator(StorageDomain dom) {
        return new StorageDomainToPoolRelationValidator(dom.getStorageStaticData(), null);
    }

    private Pair<Boolean, VdcFault> connectStorage() {
        return getStorageHelper(getStorageDomain()).connectStorageToDomainByVdsIdDetails(getStorageDomain(),
                getVds().getId());
    }

    private void disconnectStorage() {
        getStorageHelper(getStorageDomain()).disconnectStorageFromDomainByVdsId(getStorageDomain(),
                getVds().getId());
    }

    protected boolean isLocalFs(StorageDomain dom) {
        return dom.getStorageType() == StorageType.LOCALFS;
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
                        params, cloneContext().withoutCompensationContext().withoutExecutionContext()).getSucceeded();
    }

    protected boolean formatStorage(StorageDomain dom, VDS vds) {
        try {
            return runVdsCommand(VDSCommandType.FormatStorageDomain,
                            new FormatStorageDomainVDSCommandParameters(vds.getId(), dom.getId())).getSucceeded();
        } catch (VdcBLLException e) {
            if (e.getErrorCode() != VdcBllErrors.StorageDomainDoesNotExist) {
                throw e;
            }
            log.warn("Storage Domain '{}' which was about to be formatted does not exist in VDS '{}'",
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
