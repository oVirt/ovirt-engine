package org.ovirt.engine.ui.uicommonweb.models.storage;

import org.ovirt.engine.core.common.businessentities.StorageDomainType;
import org.ovirt.engine.core.common.businessentities.StorageType;
import org.ovirt.engine.core.compat.Event;
import org.ovirt.engine.core.compat.EventArgs;
import org.ovirt.engine.core.compat.EventDefinition;
import org.ovirt.engine.ui.uicommonweb.UICommand;
import org.ovirt.engine.ui.uicommonweb.models.EntityModel;
import org.ovirt.engine.ui.uicommonweb.models.Model;
import org.ovirt.engine.ui.uicommonweb.validation.IValidation;
import org.ovirt.engine.ui.uicommonweb.validation.LinuxMountPointValidation;
import org.ovirt.engine.ui.uicommonweb.validation.NonUtfValidation;
import org.ovirt.engine.ui.uicommonweb.validation.NotEmptyValidation;

@SuppressWarnings("unused")
public class NfsStorageModel extends Model implements IStorageModel
{

    public static EventDefinition PathChangedEventDefinition;
    private Event privatePathChangedEvent;

    public Event getPathChangedEvent()
    {
        return privatePathChangedEvent;
    }

    private void setPathChangedEvent(Event value)
    {
        privatePathChangedEvent = value;
    }

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

    static
    {
        PathChangedEventDefinition = new EventDefinition("PathChanged", NfsStorageModel.class); //$NON-NLS-1$
    }

    public NfsStorageModel()
    {
        setPathChangedEvent(new Event(PathChangedEventDefinition));

        setUpdateCommand(new UICommand("Update", this)); //$NON-NLS-1$

        setPath(new EntityModel());
        getPath().getEntityChangedEvent().addListener(this);
    }

    @Override
    public void eventRaised(Event ev, Object sender, EventArgs args)
    {
        super.eventRaised(ev, sender, args);

        if (ev.equals(EntityModel.EntityChangedEventDefinition) && sender == getPath())
        {
            Path_EntityChanged();
        }
    }

    private void Path_EntityChanged()
    {
        getPathChangedEvent().raise(this, EventArgs.Empty);
    }

    @Override
    public boolean Validate()
    {
        getPath().ValidateEntity(new IValidation[] {
                new NotEmptyValidation(),
                new LinuxMountPointValidation(),
                new NonUtfValidation() });

        return getPath().getIsValid();
    }

    @Override
    public StorageType getType()
    {
        return StorageType.NFS;
    }
}
