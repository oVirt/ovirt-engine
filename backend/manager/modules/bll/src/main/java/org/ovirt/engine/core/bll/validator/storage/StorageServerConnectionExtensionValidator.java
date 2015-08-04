package org.ovirt.engine.core.bll.validator.storage;

import org.ovirt.engine.core.bll.ValidationResult;
import org.ovirt.engine.core.common.businessentities.storage.StorageServerConnectionExtension;
import org.ovirt.engine.core.common.errors.EngineMessage;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dal.dbbroker.DbFacade;

public class StorageServerConnectionExtensionValidator {

    public static ValidationResult isConnectionExtensionExists(Guid connExtId) {
        StorageServerConnectionExtension connExt = DbFacade.getInstance().getStorageServerConnectionExtensionDao().get(connExtId);
        if (connExt == null) {
            return new ValidationResult(EngineMessage.ACTION_TYPE_FAILED_STORAGE_CONNECTION_EXTENSION_DOES_NOT_EXIST,
                    String.format("$%1$s %2$s", "connExtId", connExtId.toString()));
        }
        return ValidationResult.VALID;
    }
}
