package org.ovirt.engine.ui.uicommonweb.models.templates;

import org.ovirt.engine.core.common.businessentities.VmTemplate;

@SuppressWarnings("unused")
public class TemplateImportInterfaceListModel extends TemplateInterfaceListModel
{
    public TemplateImportInterfaceListModel() {
        setIsTimerDisabled(true);
    }

    @Override
    protected void OnEntityChanged()
    {
        super.OnEntityChanged();

        if (getEntity() != null)
        {
            VmTemplate template = (VmTemplate) getEntity();
            setItems(template.getInterfaces());
        }
        else
        {
            setItems(null);
        }
    }

    @Override
    protected void SyncSearch() {
    }
}
