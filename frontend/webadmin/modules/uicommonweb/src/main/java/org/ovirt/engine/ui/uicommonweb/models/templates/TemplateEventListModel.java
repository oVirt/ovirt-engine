package org.ovirt.engine.ui.uicommonweb.models.templates;

import org.ovirt.engine.core.common.businessentities.VmTemplate;
import org.ovirt.engine.ui.uicommonweb.models.events.SubTabEventListModel;
import org.ovirt.engine.ui.uicompat.PropertyChangedEventArgs;

public class TemplateEventListModel extends SubTabEventListModel
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
            getSearchCommand().execute();
        }
        else
        {
            setItems(null);
        }
    }

    @Override
    public void search()
    {
        if (getEntity() != null)
        {
            preSearchCalled(getEntity());
            super.search();
        }
    }

    protected void preSearchCalled(VmTemplate template) {
        setSearchString("Events:event_template=" + template.getName()); //$NON-NLS-1$
    }

    @Override
    protected void entityPropertyChanged(Object sender, PropertyChangedEventArgs e)
    {
        super.entityPropertyChanged(sender, e);

        if (e.PropertyName.equals("name")) //$NON-NLS-1$
        {
            getSearchCommand().execute();
        }
    }
}
