package org.ovirt.engine.core.bll.storage;

import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.ovirt.engine.core.bll.RenamedEntityInfoProvider;
import org.ovirt.engine.core.bll.validator.storage.StoragePoolValidator;
import org.ovirt.engine.core.common.AuditLogType;
import org.ovirt.engine.core.common.VdcObjectType;
import org.ovirt.engine.core.common.action.StorageDomainManagementParameter;
import org.ovirt.engine.core.common.businessentities.StorageDomainStatic;
import org.ovirt.engine.core.common.businessentities.StorageDomainStatus;
import org.ovirt.engine.core.common.errors.VdcBllMessages;
import org.ovirt.engine.core.common.validation.group.UpdateEntity;
import org.ovirt.engine.core.common.vdscommands.SetStorageDomainDescriptionVDSCommandParameters;
import org.ovirt.engine.core.common.vdscommands.VDSCommandType;
import org.ovirt.engine.core.dal.dbbroker.auditloghandling.AuditLogableBase;
import org.ovirt.engine.core.utils.ObjectIdentityChecker;

public class UpdateStorageDomainCommand<T extends StorageDomainManagementParameter> extends
        StorageDomainManagementCommandBase<T>  implements RenamedEntityInfoProvider {
    public UpdateStorageDomainCommand(T parameters) {
        super(parameters);
    }

    private boolean _storageDomainNameChanged;
    private StorageDomainStatic oldDomain;

    @Override
    protected boolean canDoAction() {
        if (!super.canDoAction() || !checkStorageDomain()
                || !checkStorageDomainStatus(StorageDomainStatus.Active) || !checkStorageDomainNameLengthValid()) {
            return false;
        }

        // Only after validating the existing of the storage domain in DB, we set the field lastTimeUsedAsMaster in the
        // storage domain which is about to be updated.
        oldDomain = getStorageDomainStaticDAO().get(getStorageDomain().getId());
        getStorageDomain().setLastTimeUsedAsMaster(oldDomain.getLastTimeUsedAsMaster());

        // Collect changed fields to update in a list.
        List<String> props = ObjectIdentityChecker.GetChangedFields(oldDomain, getStorageDomain()
                .getStorageStaticData());

        // Allow change only to name, description, comment and wipe after delete fields.
        props.remove("storageName");
        props.remove("description");
        props.remove("comment");
        props.remove("wipeAfterDelete");
        if (!props.isEmpty()) {
            log.warn("There was an attempt to update the following fields although they are not allowed to be updated: {}",
                    StringUtils.join(props, ","));
            return failCanDoAction(VdcBllMessages.ERROR_CANNOT_CHANGE_STORAGE_DOMAIN_FIELDS);
        }

        _storageDomainNameChanged =
                !StringUtils.equals(oldDomain.getStorageName(), getStorageDomain().getStorageName());
        // if domain is part of pool, and name changed, check that pool is up in
        // order to change description in spm
        if (_storageDomainNameChanged && !validate(new StoragePoolValidator(getStoragePool()).isUp())) {
            return false;
        }

        if (_storageDomainNameChanged && isStorageWithSameNameExists()) {
            return failCanDoAction(VdcBllMessages.ACTION_TYPE_FAILED_STORAGE_DOMAIN_NAME_ALREADY_EXIST);
        }

        return true;
    }

    @Override
    protected void setActionMessageParameters() {
        super.setActionMessageParameters();
        addCanDoActionMessage(VdcBllMessages.VAR__ACTION__UPDATE);
    }

    @Override
    public AuditLogType getAuditLogTypeValue() {
        return getSucceeded() ? AuditLogType.USER_UPDATE_STORAGE_DOMAIN
                : AuditLogType.USER_UPDATE_STORAGE_DOMAIN_FAILED;
    }

    @Override
    protected void executeCommand() {
        getStorageDomainStaticDAO().update(getStorageDomain().getStorageStaticData());
        if (_storageDomainNameChanged && getStoragePool() != null) {
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
