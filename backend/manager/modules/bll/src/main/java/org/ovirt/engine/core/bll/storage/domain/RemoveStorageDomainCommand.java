package org.ovirt.engine.core.bll.storage.domain;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;

import org.ovirt.engine.core.bll.LockMessagesMatchUtil;
import org.ovirt.engine.core.bll.NonTransactiveCommandAttribute;
import org.ovirt.engine.core.bll.context.CommandContext;
import org.ovirt.engine.core.bll.validator.storage.StorageDomainToPoolRelationValidator;
import org.ovirt.engine.core.common.AuditLogType;
import org.ovirt.engine.core.common.action.ActionType;
import org.ovirt.engine.core.common.action.DetachStorageDomainFromPoolParameters;
import org.ovirt.engine.core.common.action.LockProperties;
import org.ovirt.engine.core.common.action.LockProperties.Scope;
import org.ovirt.engine.core.common.action.RemoveStorageDomainParameters;
import org.ovirt.engine.core.common.businessentities.StorageDomain;
import org.ovirt.engine.core.common.businessentities.StoragePoolIsoMapId;
import org.ovirt.engine.core.common.businessentities.VDS;
import org.ovirt.engine.core.common.businessentities.VDSStatus;
import org.ovirt.engine.core.common.businessentities.storage.StorageType;
import org.ovirt.engine.core.common.errors.EngineError;
import org.ovirt.engine.core.common.errors.EngineException;
import org.ovirt.engine.core.common.errors.EngineFault;
import org.ovirt.engine.core.common.errors.EngineMessage;
import org.ovirt.engine.core.common.locks.LockingGroup;
import org.ovirt.engine.core.common.queries.QueryType;
import org.ovirt.engine.core.common.queries.StorageDomainsAndStoragePoolIdQueryParameters;
import org.ovirt.engine.core.common.utils.Pair;
import org.ovirt.engine.core.common.vdscommands.FormatStorageDomainVDSCommandParameters;
import org.ovirt.engine.core.common.vdscommands.VDSCommandType;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dao.StorageDomainDao;
import org.ovirt.engine.core.dao.StoragePoolIsoMapDao;
import org.ovirt.engine.core.utils.transaction.TransactionSupport;

@NonTransactiveCommandAttribute
public class RemoveStorageDomainCommand<T extends RemoveStorageDomainParameters> extends StorageDomainCommandBase<T> {

    @Inject
    private StoragePoolIsoMapDao storagePoolIsoMapDao;
    @Inject
    private StorageDomainDao storageDomainDao;

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

        if (detachLocalStorageDomain(dom)) {
            return;
        }

        if (format) {
            Pair<Boolean, EngineFault> connectResult = connectStorage();
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

        TransactionSupport.executeInNewTransaction(() -> {
            getStorageHelper(dom).storageDomainRemoved(dom.getStorageStaticData());
            storageDomainDao.remove(dom.getId());
            return null;
        });

        setSucceeded(true);
    }

    private boolean detachLocalStorageDomain(StorageDomain dom) {
        return isLocalFs(dom) && isDomainAttached(dom) && !detachStorage(dom);
    }

    @Override
    public AuditLogType getAuditLogTypeValue() {
        return getSucceeded() ? AuditLogType.USER_REMOVE_STORAGE_DOMAIN
                : AuditLogType.USER_REMOVE_STORAGE_DOMAIN_FAILED;
    }

    @Override
    protected void setActionMessageParameters() {
        super.setActionMessageParameters();
        addValidationMessage(EngineMessage.VAR__ACTION__REMOVE);
    }

    @Override
    protected boolean validate() {
        if (!super.validate()) {
            return false;
        }

        StorageDomain dom = getStorageDomain();
        if (dom == null) {
            return failValidation(EngineMessage.ACTION_TYPE_FAILED_STORAGE_DOMAIN_NOT_EXIST);
        }

        if (!dom.isManaged()) {
            return true;
        }

        VDS vds = getVds();
        boolean localFs = isLocalFs(dom);

        if (vds == null) {
            if (localFs) {
                if (!initializeVds()) {
                    return false;
                }
            } else {
                return failValidation(EngineMessage.CANNOT_REMOVE_STORAGE_DOMAIN_INVALID_HOST_ID);
            }
        } else if (vds.getStatus() != VDSStatus.Up) {
                return failValidation(EngineMessage.CANNOT_REMOVE_STORAGE_DOMAIN_HOST_NOT_UP,
                        String.format("$%1$s %2$s", "hostName", vds.getName()));
        }

        StorageDomainToPoolRelationValidator domainPoolValidator = createDomainToPoolValidator(dom);

        if (!checkStorageDomain()) {
            return false;
        }

        if (!isSupportedByManagedBlockStorageDomain(dom)) {
            return false;
        }

        if (!localFs && !validate(domainPoolValidator.isStorageDomainNotInAnyPool())) {
            return false;
        }

        if (localFs && isDomainAttached(dom) && !canDetachDomain(getParameters().getDestroyingPool())) {
            return false;
        }

        if (getParameters().getDoFormat()) {
            if (dom.getStorageType().isManagedBlockStorage()) {
                return failValidation(EngineMessage.ACTION_TYPE_FAILED_CANNOT_FORMAT_MANAGED_BLOCK_STORAGE_DOMAIN);
            } else if (!localFs && isStorageDomainAttached(dom)) {
                return failValidation(EngineMessage.ACTION_TYPE_FAILED_FORMAT_STORAGE_DOMAIN_WITH_ATTACHED_DATA_DOMAIN);
            }
        }

        if (dom.getStorageType().isOpenStackDomain()) {
            return failValidation(EngineMessage.ERROR_CANNOT_MANAGE_STORAGE_DOMAIN);
        }

        return true;
    }

    protected boolean isStorageDomainAttached(StorageDomain dom) {
        List<StorageDomain> storageDomainList =
                backend.runInternalQuery(QueryType.GetStorageDomainsWithAttachedStoragePoolGuid,
                        new StorageDomainsAndStoragePoolIdQueryParameters(dom, getStoragePoolId(), getVds().getId(), false))
                        .getReturnValue();
        return !storageDomainList.isEmpty();
    }

    protected StorageDomainToPoolRelationValidator createDomainToPoolValidator(StorageDomain dom) {
        return new StorageDomainToPoolRelationValidator(dom.getStorageStaticData(), null);
    }

    private Pair<Boolean, EngineFault> connectStorage() {
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

        return storagePoolIsoMapDao.get(new StoragePoolIsoMapId(storageDomainId, storagePoolId)) != null;
    }

    protected boolean detachStorage(StorageDomain dom) {
        Guid domId = dom.getId();
        Guid poolId = dom.getStoragePoolId();
        DetachStorageDomainFromPoolParameters params = new DetachStorageDomainFromPoolParameters(domId, poolId);
        params.setDestroyingPool(getParameters().getDestroyingPool());

        return backend
                .runInternalAction(ActionType.DetachStorageDomainFromPool,
                        params, cloneContext().withoutCompensationContext().withoutExecutionContext()).getSucceeded();
    }

    protected boolean formatStorage(StorageDomain dom, VDS vds) {
        try {
            return runVdsCommand(VDSCommandType.FormatStorageDomain,
                            new FormatStorageDomainVDSCommandParameters(vds.getId(), dom.getId())).getSucceeded();
        } catch (EngineException e) {
            if (e.getErrorCode() != EngineError.StorageDomainDoesNotExist) {
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
                LockMessagesMatchUtil.makeLockingPair(LockingGroup.STORAGE, EngineMessage.ACTION_TYPE_FAILED_OBJECT_LOCKED));
    }
}
