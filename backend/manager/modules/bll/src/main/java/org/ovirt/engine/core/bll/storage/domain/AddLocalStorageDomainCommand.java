package org.ovirt.engine.core.bll.storage.domain;

import javax.inject.Inject;

import org.ovirt.engine.core.bll.context.CommandContext;
import org.ovirt.engine.core.common.action.ActionReturnValue;
import org.ovirt.engine.core.common.action.ActionType;
import org.ovirt.engine.core.common.action.AttachStorageDomainToPoolParameters;
import org.ovirt.engine.core.common.action.StorageDomainManagementParameter;
import org.ovirt.engine.core.common.businessentities.StoragePool;
import org.ovirt.engine.core.common.businessentities.StoragePoolStatus;
import org.ovirt.engine.core.common.businessentities.storage.StorageType;
import org.ovirt.engine.core.common.errors.EngineMessage;
import org.ovirt.engine.core.common.utils.VersionStorageFormatUtil;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dao.StoragePoolDao;
import org.ovirt.engine.core.dao.StorageServerConnectionDao;

public class AddLocalStorageDomainCommand<T extends StorageDomainManagementParameter> extends AddStorageDomainCommon<T> {

    @Inject
    private StorageServerConnectionDao storageServerConnectionDao;
    @Inject
    private StoragePoolDao storagePoolDao;

    /**
     * Constructor for command creation when compensation is applied on startup
     */
    public AddLocalStorageDomainCommand(Guid commandId) {
        super(commandId);
    }

    public AddLocalStorageDomainCommand(T parameters, CommandContext commandContext) {
        super(parameters, commandContext);
    }

    @Override
    protected boolean validate() {
        if (!super.validate()) {
            return false;
        }

        StoragePool storagePool = storagePoolDao.getForVds(getParameters().getVdsId());

        if (storagePool == null) {
            return failValidation(EngineMessage.NETWORK_CLUSTER_HAVE_NOT_EXISTING_DATA_CENTER_NETWORK);
        }

        setStoragePool(storagePool);

        if (getStorageDomain().getStorageType() == StorageType.LOCALFS && !storagePool.isLocal()) {
            return failValidation(EngineMessage.ACTION_TYPE_FAILED_STORAGE_POOL_IS_NOT_LOCAL);
        }

        if (VersionStorageFormatUtil.getForVersion(storagePool.getCompatibilityVersion())
                .compareTo(getStorageDomain().getStorageFormat()) < 0) {
            return failValidation(EngineMessage.ERROR_CANNOT_ADD_STORAGE_POOL_WITH_DIFFERENT_STORAGE_FORMAT);
        }

        if (storagePool.getStatus() != StoragePoolStatus.Uninitialized) {
            if (!checkMasterDomainIsUp()) {
                return false;
            }
        }

        return true;
    }

    @Override
    protected void executeCommand() {
        super.executeCommand();
        if (getSucceeded()) {
            ActionReturnValue returnValue = backend
                    .runInternalAction(
                            ActionType.AttachStorageDomainToPool,
                            new AttachStorageDomainToPoolParameters(getStorageDomain().getId(), getStoragePool().getId()));
            if(!returnValue.getSucceeded()) {
                getReturnValue().setSucceeded(false);
                getReturnValue().setFault(returnValue.getFault());
            }
        }
    }
}
