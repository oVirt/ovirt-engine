package org.ovirt.engine.core.bll.storage.domain;

import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.ovirt.engine.core.bll.RenamedEntityInfoProvider;
import org.ovirt.engine.core.bll.context.CommandContext;
import org.ovirt.engine.core.bll.validator.storage.StoragePoolValidator;
import org.ovirt.engine.core.common.AuditLogType;
import org.ovirt.engine.core.common.VdcObjectType;
import org.ovirt.engine.core.common.action.StorageDomainManagementParameter;
import org.ovirt.engine.core.common.businessentities.StorageDomainStatic;
import org.ovirt.engine.core.common.businessentities.StorageDomainStatus;
import org.ovirt.engine.core.common.errors.EngineMessage;
import org.ovirt.engine.core.common.validation.group.UpdateEntity;
import org.ovirt.engine.core.common.vdscommands.SetStorageDomainDescriptionVDSCommandParameters;
import org.ovirt.engine.core.common.vdscommands.VDSCommandType;
import org.ovirt.engine.core.dal.dbbroker.auditloghandling.AuditLogableBase;
import org.ovirt.engine.core.utils.ObjectIdentityChecker;

public class UpdateStorageDomainCommand<T extends StorageDomainManagementParameter> extends
        StorageDomainManagementCommandBase<T>  implements RenamedEntityInfoProvider {

    public UpdateStorageDomainCommand(T parameters, CommandContext commandContext) {
        super(parameters, commandContext);
    }

    private boolean storageDomainNameChanged;
    private StorageDomainStatic oldDomain;

    @Override
    protected boolean validate() {
        if (!super.validate() || !checkStorageDomain()) {
            return false;
        }

        // Only after validating the existence of the storage domain in DB, we set the field lastTimeUsedAsMaster in the
        // storage domain which is about to be updated.
        oldDomain = getStorageDomainStaticDao().get(getStorageDomain().getId());
        getStorageDomain().setLastTimeUsedAsMaster(oldDomain.getLastTimeUsedAsMaster());

        return validateStoragePropertiesUpdate();
    }

    private boolean validateStoragePropertiesUpdate() {
       if (!checkStorageDomainStatusNotEqual(StorageDomainStatus.Locked) || !validateStorageNameUpdate()) {
           return false;
       }

        // Collect changed fields to update in a list.
        List<String> props = ObjectIdentityChecker.getChangedFields(oldDomain, getStorageDomain()
                .getStorageStaticData());

        // Allow changes to the following fields only:
        props.remove("storageName");
        props.remove("description");
        props.remove("comment");
        props.remove("wipeAfterDelete");
        props.remove("warningLowSpaceIndicator");
        props.remove("criticalSpaceActionBlocker");
        if (!props.isEmpty()) {
            log.warn("There was an attempt to update the following fields although they are not allowed to be updated: {}",
                    StringUtils.join(props, ","));
            return failValidation(EngineMessage.ERROR_CANNOT_CHANGE_STORAGE_DOMAIN_FIELDS);
        }
        return true;
    }

    private boolean validateStorageNameUpdate() {
        storageDomainNameChanged =
                !StringUtils.equals(oldDomain.getStorageName(), getStorageDomain().getStorageName());

        if (!storageDomainNameChanged) {
            return true;
        }

        return checkStorageDomainStatus(StorageDomainStatus.Active)
                && checkStorageDomainNameLengthValid()
                && isPoolUp()
                && validateNotTheSameName();
    }

    private boolean isPoolUp() {
        // if domain is part of pool, and name changed, check that pool is up in
        // order to change description in spm
        return validate(new StoragePoolValidator(getStoragePool()).isUp());
    }

    private boolean validateNotTheSameName() {
        if (isStorageWithSameNameExists()) {
            return failValidation(EngineMessage.ACTION_TYPE_FAILED_STORAGE_DOMAIN_NAME_ALREADY_EXIST);
        }
        return true;
    }

    @Override
    protected void setActionMessageParameters() {
        super.setActionMessageParameters();
        addValidationMessage(EngineMessage.VAR__ACTION__UPDATE);
    }

    @Override
    public AuditLogType getAuditLogTypeValue() {
        return getSucceeded() ? AuditLogType.USER_UPDATE_STORAGE_DOMAIN
                : AuditLogType.USER_UPDATE_STORAGE_DOMAIN_FAILED;
    }

    @Override
    protected void executeCommand() {
        getStorageDomainStaticDao().update(getStorageDomain().getStorageStaticData());
        if (storageDomainNameChanged && getStoragePool() != null) {
            runVdsCommand(
                            VDSCommandType.SetStorageDomainDescription,
                            new SetStorageDomainDescriptionVDSCommandParameters(getStoragePool().getId(),
                                    getStorageDomain().getId(), getStorageDomain().getStorageName()));
        }
        setSucceeded(true);
    }

    @Override
    protected List<Class<?>> getValidationGroups() {
        addValidationGroup(UpdateEntity.class);
        return super.getValidationGroups();
    }

    @Override
    public String getEntityType() {
        return VdcObjectType.Storage.getVdcObjectTranslation();
    }

    @Override
    public String getEntityOldName() {
        return oldDomain.getStorageName();
    }

    @Override
    public String getEntityNewName() {
        return getStorageDomain().getStorageName();
    }

    @Override
    public void setEntityId(AuditLogableBase logable) {
        logable.setStorageDomainId(oldDomain.getId());
    }
}
