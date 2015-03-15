package org.ovirt.engine.core.bll.storage;

import java.util.Collection;
import java.util.List;

import org.apache.commons.collections.CollectionUtils;
import org.ovirt.engine.core.bll.validator.storage.StorageConnectionValidator;
import org.ovirt.engine.core.common.action.AttachDetachStorageConnectionParameters;
import org.ovirt.engine.core.common.businessentities.StorageServerConnections;
import org.ovirt.engine.core.common.businessentities.storage.LUNs;
import org.ovirt.engine.core.common.errors.VdcBllMessages;

public class DetachStorageConnectionFromStorageDomainCommand<T extends AttachDetachStorageConnectionParameters>
        extends StorageDomainCommandBase<T> {

    public DetachStorageConnectionFromStorageDomainCommand(T parameters) {
        super(parameters);
    }

    @Override
    protected boolean canDoAction() {
        StorageConnectionValidator storageConnectionValidator = createStorageConnectionValidator();

        if (!validate(storageConnectionValidator.isConnectionExists())
                || !validate(storageConnectionValidator.isDomainOfConnectionExistsAndInactive(getStorageDomain()))
                || !validate(storageConnectionValidator.isISCSIConnectionAndDomain(getStorageDomain()))) {
            return false;
        }
        if(!storageConnectionValidator.isConnectionForISCSIDomainAttached(getStorageDomain())) {
            return failCanDoAction(VdcBllMessages.ACTION_TYPE_FAILED_STORAGE_CONNECTION_FOR_DOMAIN_NOT_EXIST);
        }
        return true;
    }

    protected StorageConnectionValidator createStorageConnectionValidator() {
        String connectionId = getParameters().getStorageConnectionId();
        StorageServerConnections connection = getStorageServerConnectionDAO().get(connectionId);

        return new StorageConnectionValidator(connection);
    }

    @Override
    protected void executeCommand() {
        String connectionId = getParameters().getStorageConnectionId();
        List<LUNs> lunsForConnection = getLunDao().getAllForStorageServerConnection(connectionId);
        List<LUNs> lunsForVG = getLunDao().getAllForVolumeGroup(getStorageDomain().getStorage());
        Collection<LUNs> lunsToRemove = (Collection<LUNs>) CollectionUtils.intersection(lunsForConnection, lunsForVG);

        for (LUNs lun : lunsToRemove) {
            if (getLunDao().get(lun.getLUN_id()) != null) {
                getLunDao().remove(lun.getLUN_id());
            }
        }

        setSucceeded(true);
    }

    @Override
    protected void setActionMessageParameters() {
        addCanDoActionMessage(VdcBllMessages.VAR__ACTION__DETACH);
        addCanDoActionMessage(VdcBllMessages.VAR__TYPE__STORAGE__CONNECTION);
    }

}
