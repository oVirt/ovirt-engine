package org.ovirt.engine.ui.uicommonweb.models.templates;

import org.ovirt.engine.core.common.businessentities.DiskImage;
import org.ovirt.engine.core.common.businessentities.VmTemplate;

@SuppressWarnings("unused")
public class TemplateImportDiskListModel extends TemplateDiskListModel
{
    @Override
    protected void OnEntityChanged()
    {
        super.OnEntityChanged();

        if (getEntity() != null)
        {
            java.util.Map.Entry<VmTemplate, java.util.ArrayList<DiskImage>> pair =
                    (java.util.Map.Entry<VmTemplate, java.util.ArrayList<DiskImage>>) getEntity();
            setItems(pair.getValue());
        }
        else
        {
            setItems(null);
        }
    }
}
