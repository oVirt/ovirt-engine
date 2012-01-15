package org.ovirt.engine.ui.uicommonweb.models.templates;

import org.ovirt.engine.core.common.businessentities.DiskImage;
import org.ovirt.engine.core.common.businessentities.VmTemplate;

@SuppressWarnings("unused")
public class TemplateImportInterfaceListModel extends TemplateInterfaceListModel
{
    @Override
    protected void OnEntityChanged()
    {
        super.OnEntityChanged();

        if (getEntity() != null)
        {
            java.util.Map.Entry<VmTemplate, java.util.ArrayList<DiskImage>> pair =
                    (java.util.Map.Entry<VmTemplate, java.util.ArrayList<DiskImage>>) getEntity();
            VmTemplate template = pair.getKey();
            setItems(template.getInterfaces());
        }
        else
        {
            setItems(null);
        }
    }
}
