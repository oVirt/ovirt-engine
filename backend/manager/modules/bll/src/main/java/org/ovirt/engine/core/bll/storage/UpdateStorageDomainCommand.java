package org.ovirt.engine.core.bll.storage;

import java.util.List;

import org.ovirt.engine.core.bll.Backend;
import org.ovirt.engine.core.common.AuditLogType;
import org.ovirt.engine.core.common.action.StorageDomainManagementParameter;
import org.ovirt.engine.core.common.businessentities.StorageDomainStatus;
import org.ovirt.engine.core.common.businessentities.storage_domain_static;
import org.ovirt.engine.core.common.validation.group.UpdateEntity;
import org.ovirt.engine.core.common.vdscommands.IrsBaseVDSCommandParameters;
import org.ovirt.engine.core.common.vdscommands.SetStorageDomainDescriptionVDSCommandParameters;
import org.ovirt.engine.core.common.vdscommands.VDSCommandType;
import org.ovirt.engine.core.compat.StringHelper;
import org.ovirt.engine.core.dal.VdcBllMessages;
import org.ovirt.engine.core.dal.dbbroker.DbFacade;
import org.ovirt.engine.core.utils.ObjectIdentityChecker;

public class UpdateStorageDomainCommand<T extends StorageDomainManagementParameter> extends
        StorageDomainManagementCommandBase<T> {
    public UpdateStorageDomainCommand(T parameters) {
        super(parameters);
    }

    private boolean _storageDomainNameChanged;

    @Override
    protected boolean canDoAction() {
        addCanDoActionMessage(VdcBllMessages.VAR__ACTION__UPDATE);
        boolean returnValue = super.canDoAction() && CheckStorageDomain()
                && checkStorageDomainStatus(StorageDomainStatus.Active) && CheckStorageDomainNameLengthValid();
        storage_domain_static oldDomain =
                DbFacade.getInstance().getStorageDomainStaticDAO().get(getStorageDomain().getId());

        java.util.ArrayList<String> props = ObjectIdentityChecker.GetChangedFields(oldDomain, getStorageDomain()
                .getStorageStaticData());
        // allow change only to name field
        props.remove("storage_name");
        if (returnValue && props.size() > 0) {
            addCanDoActionMessage(VdcBllMessages.ERROR_CANNOT_CHANGE_STORAGE_DOMAIN_FIELDS);
            returnValue = false;
        }
        _storageDomainNameChanged = !StringHelper.EqOp(oldDomain.getstorage_name(), getStorageDomain()
                .getstorage_name());
        // if domain is part of pool, and name changed, check that pool is up in
        // order to change description in spm
        if (returnValue
                && _storageDomainNameChanged
                && getStoragePool() != null
                && !((Boolean) Backend
                        .getInstance()
                        .getResourceManager()
                        .RunVdsCommand(VDSCommandType.IsValid,
                                new IrsBaseVDSCommandParameters(getStoragePool().getId())).getReturnValue())
                        .booleanValue()) {
            addCanDoActionMessage(VdcBllMessages.ACTION_TYPE_FAILED_IMAGE_REPOSITORY_NOT_FOUND);
            returnValue = false;
        }
        if (returnValue && _storageDomainNameChanged && IsStorageWithSameNameExists()) {
            addCanDoActionMessage(VdcBllMessages.ACTION_TYPE_FAILED_STORAGE_DOMAIN_NAME_ALREADY_EXIST);
            returnValue = false;
        }
        return returnValue;
    }

    @Override
    public AuditLogType getAuditLogTypeValue() {
        return getSucceeded() ? AuditLogType.USER_UPDATE_STORAGE_DOMAIN
                : AuditLogType.USER_UPDATE_STORAGE_DOMAIN_FAILED;
    }

    @Override
    protected void executeCommand() {
        DbFacade.getInstance().getStorageDomainStaticDAO().update(getStorageDomain().getStorageStaticData());
        if (_storageDomainNameChanged && getStoragePool() != null) {
            Backend.getInstance()
                    .getResourceManager()
                    .RunVdsCommand(
                            VDSCommandType.SetStorageDomainDescription,
                            new SetStorageDomainDescriptionVDSCommandParameters(getStoragePool().getId(),
                                    getStorageDomain().getId(), getStorageDomain().getstorage_name()));
        }
        setSucceeded(true);
    }

    @Override
    protected List<Class<?>> getValidationGroups() {
        addValidationGroup(UpdateEntity.class);
        return super.getValidationGroups();
    }

}
