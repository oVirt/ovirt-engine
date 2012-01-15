package org.ovirt.engine.ui.uicommonweb.models.storage;

import org.ovirt.engine.core.common.businessentities.StorageDomainType;
import org.ovirt.engine.core.common.businessentities.StorageType;
import org.ovirt.engine.ui.uicommonweb.UICommand;
import org.ovirt.engine.ui.uicommonweb.models.EntityModel;
import org.ovirt.engine.ui.uicommonweb.models.Model;
import org.ovirt.engine.ui.uicommonweb.validation.NotEmptyValidation;

@SuppressWarnings("unused")
public class LocalStorageModel extends Model implements IStorageModel
{

    private UICommand privateUpdateCommand;

    @Override
    public UICommand getUpdateCommand()
    {
        return privateUpdateCommand;
    }

    private void setUpdateCommand(UICommand value)
    {
        privateUpdateCommand = value;
    }

    private StorageModel privateContainer;

    @Override
    public StorageModel getContainer()
    {
        return privateContainer;
    }

    @Override
    public void setContainer(StorageModel value)
    {
        privateContainer = value;
    }

    private StorageDomainType privateRole = StorageDomainType.values()[0];

    @Override
    public StorageDomainType getRole()
    {
        return privateRole;
    }

    @Override
    public void setRole(StorageDomainType value)
    {
        privateRole = value;
    }

    private EntityModel privatePath;

    public EntityModel getPath()
    {
        return privatePath;
    }

    public void setPath(EntityModel value)
    {
        privatePath = value;
    }

    public LocalStorageModel()
    {
        setUpdateCommand(new UICommand("Update", this));

        setPath(new EntityModel());
    }

    @Override
    public boolean Validate()
    {
        getPath().ValidateEntity(new NotEmptyValidation[] { new NotEmptyValidation() });

        return getPath().getIsValid();
    }

    @Override
    public StorageType getType()
    {
        return StorageType.LOCALFS;
    }
}
