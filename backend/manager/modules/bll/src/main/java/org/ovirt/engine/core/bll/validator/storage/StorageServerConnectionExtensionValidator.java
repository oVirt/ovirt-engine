package org.ovirt.engine.core.bll.validator.storage;

import org.ovirt.engine.core.bll.ValidationResult;
import org.ovirt.engine.core.common.businessentities.VDS;
import org.ovirt.engine.core.common.businessentities.storage.StorageServerConnectionExtension;
import org.ovirt.engine.core.common.errors.EngineMessage;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dal.dbbroker.DbFacade;

public class StorageServerConnectionExtensionValidator {

    public ValidationResult isConnectionExtensionExists(Guid connExtId) {
        StorageServerConnectionExtension connExt = getDbFacade().getStorageServerConnectionExtensionDao().get(connExtId);
        if (connExt == null) {
            return new ValidationResult(EngineMessage.ACTION_TYPE_FAILED_STORAGE_CONNECTION_EXTENSION_DOES_NOT_EXIST,
                    String.format("$%1$s %2$s", "connExtId", connExtId.toString()));
        }
        return ValidationResult.VALID;
    }

    public ValidationResult isConnectionDoesNotExistForHostAndTarget(StorageServerConnectionExtension connExt) {
        StorageServerConnectionExtension existingConnExt = getDbFacade().getStorageServerConnectionExtensionDao().getByHostIdAndTarget(
                connExt.getHostId(),
                connExt.getIqn());
        if (existingConnExt != null && !existingConnExt.getId().equals(connExt.getId())) {
            VDS host = getDbFacade().getVdsDao().get(connExt.getHostId());
            return new ValidationResult(EngineMessage.ACTION_TYPE_FAILED_STORAGE_CONNECTION_EXTENSION_ALREADY_EXISTS,
                    String.format("$target %s", connExt.getIqn()),
                    String.format("$vdsName %s", host.getName()));
        }
        return ValidationResult.VALID;
    }

    protected DbFacade getDbFacade() {
        return DbFacade.getInstance();
    }
}
