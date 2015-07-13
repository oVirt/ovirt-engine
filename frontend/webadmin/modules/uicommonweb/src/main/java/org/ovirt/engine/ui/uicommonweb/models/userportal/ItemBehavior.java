package org.ovirt.engine.ui.uicommonweb.models.userportal;

import org.ovirt.engine.ui.uicommonweb.UICommand;
import org.ovirt.engine.ui.uicompat.Event;
import org.ovirt.engine.ui.uicompat.EventArgs;
import org.ovirt.engine.ui.uicompat.PropertyChangedEventArgs;

@SuppressWarnings("unused")
public abstract class ItemBehavior {
    private UserPortalItemModel privateItem;

    protected UserPortalItemModel getItem() {
        return privateItem;
    }

    private void setItem(UserPortalItemModel value) {
        privateItem = value;
    }

    protected ItemBehavior(UserPortalItemModel item) {
        setItem(item);
    }

    public abstract void onEntityChanged();

    public abstract void entityPropertyChanged(PropertyChangedEventArgs e);

    public abstract void executeCommand(UICommand command);

    public abstract void eventRaised(Event ev, Object sender, EventArgs args);
}
