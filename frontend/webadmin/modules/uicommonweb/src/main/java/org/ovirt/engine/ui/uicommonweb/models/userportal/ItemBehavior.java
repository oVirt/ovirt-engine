package org.ovirt.engine.ui.uicommonweb.models.userportal;

import org.ovirt.engine.core.compat.Event;
import org.ovirt.engine.core.compat.EventArgs;
import org.ovirt.engine.core.compat.PropertyChangedEventArgs;
import org.ovirt.engine.ui.uicommonweb.UICommand;

@SuppressWarnings("unused")
public abstract class ItemBehavior
{
    private UserPortalItemModel privateItem;

    protected UserPortalItemModel getItem()
    {
        return privateItem;
    }

    private void setItem(UserPortalItemModel value)
    {
        privateItem = value;
    }

    protected ItemBehavior(UserPortalItemModel item)
    {
        setItem(item);
    }

    public abstract void OnEntityChanged();

    public abstract void EntityPropertyChanged(PropertyChangedEventArgs e);

    public abstract void ExecuteCommand(UICommand command);

    public abstract void eventRaised(Event ev, Object sender, EventArgs args);
}
