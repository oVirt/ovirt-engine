package org.ovirt.engine.ui.uicommonweb.models.storage;

import org.ovirt.engine.core.common.businessentities.StorageDomain;
import org.ovirt.engine.core.common.businessentities.StorageDomainSharedStatus;
import org.ovirt.engine.core.common.businessentities.StorageDomainStatus;
import org.ovirt.engine.core.common.businessentities.StorageServerConnections;
import org.ovirt.engine.ui.uicommonweb.dataprovider.AsyncDataProvider;
import org.ovirt.engine.ui.uicommonweb.models.EntityModel;
import org.ovirt.engine.ui.uicommonweb.models.Model;

@SuppressWarnings("unused")
public abstract class FileStorageModel extends Model implements IStorageModel{
    private EntityModel<String> path;

    protected void setPath(EntityModel<String> value) {
        path = value;
    }

    public EntityModel<String> getPath() {
        return path;
    }

    public boolean isEditable(StorageDomain storage) {
        return storage.getStatus() == StorageDomainStatus.Maintenance
                || storage.getStorageDomainSharedStatus() == StorageDomainSharedStatus.Unattached;
    }

    protected abstract void prepareConnectionForEditing (StorageServerConnections connection);

    public void prepareForEdit(StorageDomain storage) {
        boolean isEditable = isEditable(storage);
        getPath().setIsChangeable(isEditable);

        AsyncDataProvider.getInstance().getStorageConnectionById(new AsyncQuery<>(connection -> {
            getPath().setEntity(connection.getConnection());
            prepareConnectionForEditing(connection);
        }), storage.getStorage(), true);

        setHostChangeability(isEditable);
    }

    protected void setHostChangeability(boolean isPathEditable) {
        //when storage is active, only SPM can perform actions on it, thus it is set above that host is not changeable.
        //If storage is editable but not active (maintenance) - any host can perform the edit so the changeable here is set based on that
        getContainer().getHost().setIsChangeable(isPathEditable);
    }
}
