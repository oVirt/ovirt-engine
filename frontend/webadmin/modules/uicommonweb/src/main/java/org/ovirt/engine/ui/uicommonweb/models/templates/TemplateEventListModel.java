package org.ovirt.engine.ui.uicommonweb.models.templates;

import org.ovirt.engine.core.common.businessentities.VmTemplate;
import org.ovirt.engine.core.compat.PropertyChangedEventArgs;
import org.ovirt.engine.core.compat.StringFormat;
import org.ovirt.engine.ui.uicommonweb.models.events.EventListModel;

@SuppressWarnings("unused")
public class TemplateEventListModel extends EventListModel
{

    @Override
    public VmTemplate getEntity()
    {
        return (VmTemplate) ((super.getEntity() instanceof VmTemplate) ? super.getEntity() : null);
    }

    public void setEntity(VmTemplate value)
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
            setSearchString(StringFormat.format("Events:event_template=%1$s", getEntity().getname()));
            super.Search();
        }
    }

    @Override
    protected void EntityPropertyChanged(Object sender, PropertyChangedEventArgs e)
    {
        super.EntityPropertyChanged(sender, e);

        if (e.PropertyName.equals("name"))
        {
            getSearchCommand().Execute();
        }
    }
}
