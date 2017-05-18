package org.ovirt.engine.ui.uicommonweb.models.storage;

import org.ovirt.engine.core.common.businessentities.StorageDomainType;
import org.ovirt.engine.core.common.businessentities.StorageServerConnections;
import org.ovirt.engine.core.common.businessentities.storage.StorageType;
import org.ovirt.engine.ui.uicommonweb.UICommand;
import org.ovirt.engine.ui.uicommonweb.models.EntityModel;
import org.ovirt.engine.ui.uicommonweb.validation.IValidation;
import org.ovirt.engine.ui.uicommonweb.validation.LocalfsLinuxMountPointValidation;
import org.ovirt.engine.ui.uicommonweb.validation.NonUtfValidation;
import org.ovirt.engine.ui.uicommonweb.validation.NotEmptyValidation;

@SuppressWarnings("unused")
public class LocalStorageModel extends FileStorageModel {

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

    @Override
    protected void prepareConnectionForEditing(StorageServerConnections connection) {}

    public LocalStorageModel() {
        setUpdateCommand(new UICommand("Update", this)); //$NON-NLS-1$
        setPath(new EntityModel<>());
    }

    @Override
    public boolean validate() {

        getPath().validateEntity(new IValidation[] {
            new NotEmptyValidation(),
            new LocalfsLinuxMountPointValidation(),
            new NonUtfValidation() });

        return getPath().getIsValid();
    }

    @Override
    public StorageType getType() {
        return StorageType.LOCALFS;
    }
}
