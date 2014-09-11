package org.ovirt.engine.ui.uicommonweb.models.storage;

import org.ovirt.engine.core.common.businessentities.VDS;
import org.ovirt.engine.ui.uicommonweb.models.EntityModel;
import org.ovirt.engine.ui.uicommonweb.models.ListModel;
import org.ovirt.engine.ui.uicommonweb.models.Model;
import org.ovirt.engine.ui.uicompat.Event;
import org.ovirt.engine.ui.uicompat.EventArgs;

@SuppressWarnings("unused")
public class RemoveStorageModel extends Model
{

    private ListModel<VDS> privateHostList;

    public ListModel<VDS> getHostList()
    {
        return privateHostList;
    }

    private void setHostList(ListModel<VDS> value)
    {
        privateHostList = value;
    }

    private EntityModel<Boolean> privateFormat;

    public EntityModel<Boolean> getFormat()
    {
        return privateFormat;
    }

    private void setFormat(EntityModel<Boolean> value)
    {
        privateFormat = value;
    }

    public RemoveStorageModel()
    {
        setHostList(new ListModel<VDS>());

        setFormat(new EntityModel<Boolean>());
        getFormat().getEntityChangedEvent().addListener(this);
        getFormat().getPropertyChangedEvent().addListener(this);
    }

    @Override
    public void eventRaised(Event<? extends EventArgs> ev, Object sender, EventArgs args)
    {
        super.eventRaised(ev, sender, args);

        if (sender == getFormat()) {
            format_Changed(sender, args);
        }
    }

    private void format_Changed(Object sender, EventArgs args) {
        getHostList().setIsChangable(!getFormat().getIsAvailable() || Boolean.TRUE.equals(getFormat().getEntity()));
    }

    public boolean validate()
    {
        getHostList().setIsValid(getHostList().getSelectedItem() != null);

        return getHostList().getIsValid();
    }
}
