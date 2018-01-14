package org.ovirt.engine.core.bll.storage.pool;

import java.util.EnumSet;

import javax.inject.Inject;

import org.apache.commons.lang.StringUtils;
import org.ovirt.engine.core.bll.context.CommandContext;
import org.ovirt.engine.core.bll.storage.StorageHandlingCommandBase;
import org.ovirt.engine.core.common.AuditLogType;
import org.ovirt.engine.core.common.action.SetStoragePoolStatusParameters;
import org.ovirt.engine.core.common.businessentities.StorageDomainStatus;
import org.ovirt.engine.core.common.businessentities.StoragePoolStatus;
import org.ovirt.engine.core.dao.StoragePoolDao;
import org.ovirt.engine.core.vdsbroker.storage.StoragePoolDomainHelper;

public class SetStoragePoolStatusCommand<T extends SetStoragePoolStatusParameters> extends
        StorageHandlingCommandBase<T> {

    @Inject
    private StoragePoolDomainHelper storagePoolDomainHelper;
    @Inject
    private StoragePoolDao storagePoolDao;

    public SetStoragePoolStatusCommand(T parameters, CommandContext commandContext) {
        super(parameters, commandContext);
    }

    @Override
    protected void executeCommand() {
        getStoragePool().setStatus(getParameters().getStatus());
        setVdsIdRef(getStoragePool().getSpmVdsId());
        storagePoolDao.updateStatus(getStoragePool().getId(), getStoragePool().getStatus());
        if (getParameters().getStatus() == StoragePoolStatus.NonResponsive
                || getParameters().getStatus() == StoragePoolStatus.NotOperational) {
            storagePoolDomainHelper.updateApplicablePoolDomainsStatuses(getStoragePool().getId(),
                    EnumSet.of(StorageDomainStatus.Active),
                    StorageDomainStatus.Unknown,
                    null);
        }
        storagePoolStatusHandler.poolStatusChanged(getStoragePool().getId(), getStoragePool().getStatus());
        setSucceeded(true);
    }

    public String getError() {
        return backend.getVdsErrorsTranslator().translateErrorTextSingle(getParameters().getError().toString());
    }

    @Override
    public AuditLogType getAuditLogTypeValue() {
        if (getSucceeded()) {
            if (StringUtils.isEmpty(getVdsName())) {
                setVdsName("Unavailable");
            }
            return getParameters().getAuditLogType();
        } else {
            return AuditLogType.SYSTEM_FAILED_CHANGE_STORAGE_POOL_STATUS;
        }
    }

    @Override
    protected boolean validate() {
        return validate(createStoragePoolValidator().exists());
    }
}
