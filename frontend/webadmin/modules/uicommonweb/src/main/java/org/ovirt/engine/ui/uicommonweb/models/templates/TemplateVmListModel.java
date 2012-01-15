package org.ovirt.engine.ui.uicommonweb.models.templates;

import org.ovirt.engine.core.common.businessentities.VmTemplate;
import org.ovirt.engine.core.compat.StringFormat;
import org.ovirt.engine.ui.uicommonweb.models.vms.VmListModel;

@SuppressWarnings("unused")
public class TemplateVmListModel extends VmListModel
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

    public TemplateVmListModel()
    {
        setTitle("Virtual Machines");
    }

    @Override
    protected void OnEntityChanged()
    {
        super.OnEntityChanged();
        getSearchCommand().Execute();
    }

    @Override
    public void Search()
    {
        if (getEntity() != null)
        {
            setSearchString(StringFormat.format("Vms: template.name=%1$s", getEntity().getname()));
            super.Search();
        }
    }
}
