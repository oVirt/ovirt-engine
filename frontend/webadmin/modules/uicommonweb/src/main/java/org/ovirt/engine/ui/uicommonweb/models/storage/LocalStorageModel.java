package org.ovirt.engine.ui.uicommonweb.models.storage;

import org.ovirt.engine.core.common.businessentities.StorageDomainType;
import org.ovirt.engine.core.common.businessentities.StorageType;
import org.ovirt.engine.ui.uicommonweb.UICommand;
import org.ovirt.engine.ui.uicommonweb.models.EntityModel;
import org.ovirt.engine.ui.uicommonweb.models.Model;
import org.ovirt.engine.ui.uicommonweb.validation.NotEmptyValidation;

@SuppressWarnings("unused")
public class LocalStorageModel extends Model implements IStorageModel {

    private UICommand updateCommand;

    @Override
    public UICommand getUpdateCommand() {
        return updateCommand;
    }

    private void setUpdateCommand(UICommand value) {
        updateCommand = value;
    }

    private StorageModel container;

    @Override
    public StorageModel getContainer() {
        return container;
    }

    @Override
    public void setContainer(StorageModel value) {
        container = value;
    }

    private StorageDomainType role = StorageDomainType.values()[0];

    @Override
    public StorageDomainType getRole() {
        return role;
    }

    @Override
    public void setRole(StorageDomainType value) {
        role = value;
    }

    private EntityModel path;

    public EntityModel getPath() {
        return path;
    }

    public void setPath(EntityModel value) {
        path = value;
    }

    public LocalStorageModel() {

        setUpdateCommand(new UICommand("Update", this)); //$NON-NLS-1$
        setPath(new EntityModel());
    }

    @Override
    public boolean Validate() {

        getPath().ValidateEntity(new NotEmptyValidation[] { new NotEmptyValidation() });

        return getPath().getIsValid();
    }

    @Override
    public StorageType getType() {
        return StorageType.LOCALFS;
    }
}
