package org.ovirt.engine.ui.uicommonweb.models.storage;

import org.ovirt.engine.core.common.businessentities.storage_domains;
import org.ovirt.engine.core.compat.PropertyChangedEventArgs;
import org.ovirt.engine.core.compat.StringFormat;
import org.ovirt.engine.ui.uicommonweb.models.events.EventListModel;

@SuppressWarnings("unused")
public class StorageEventListModel extends EventListModel
{

    // get { return base.Entity as storage_domains; }
    @Override
    public storage_domains getEntity()
    {
        return (storage_domains) super.getEntity();
    }

    public void setEntity(storage_domains value)
    {
        super.setEntity(value);
    }

    @Override
    protected void onEntityContentChanged()
    {
        super.onEntityContentChanged();

        if (getEntity() != null)
        {
            getSearchCommand().Execute();
        }
        else
        {
            setItems(null);
        }
    }

    @Override
    public void Search()
    {
        if (getEntity() != null)
        {
            setSearchString(StringFormat.format("events: event_storage=%1$s", getEntity().getstorage_name()));
            super.Search();
        }
    }

    @Override
    protected void EntityPropertyChanged(Object sender, PropertyChangedEventArgs e)
    {
        super.EntityPropertyChanged(sender, e);

        if (e.PropertyName.equals("storage_name"))
        {
            getSearchCommand().Execute();
        }
    }
}
