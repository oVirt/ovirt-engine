package org.ovirt.engine.ui.uicommonweb.models.templates;

import java.util.ArrayList;
import java.util.Map.Entry;

import org.ovirt.engine.core.common.businessentities.DiskImage;
import org.ovirt.engine.core.common.businessentities.VmTemplate;
import org.ovirt.engine.core.common.queries.DiskImageList;

@SuppressWarnings("unused")
public class TemplateImportDiskListModel extends TemplateDiskListModel
{
    private ArrayList<Entry<VmTemplate, DiskImageList>> extendedItems;

    public TemplateImportDiskListModel() {
        setIsTimerDisabled(true);
    }

    @Override
    protected void OnEntityChanged()
    {
        super.OnEntityChanged();

        if (getEntity() != null)
        {
            if (super.getEntity() instanceof VmTemplate)
            {
                ArrayList<DiskImage> list = new ArrayList<DiskImage>();
                VmTemplate template = (VmTemplate) getEntity();
                for (Entry<VmTemplate, DiskImageList> item : extendedItems) {
                    if (item.getKey().getQueryableId().equals(template.getQueryableId())) {
                        DiskImageList diskImageList = item.getValue();
                        for (DiskImage diskImage : diskImageList.getDiskImages()) {
                            list.add(diskImage);
                        }
                        setItems(list);
                        return;
                    }
                }
            }
            else
            {
                java.util.Map.Entry<VmTemplate, java.util.ArrayList<DiskImage>> pair =
                        (java.util.Map.Entry<VmTemplate, java.util.ArrayList<DiskImage>>) getEntity();
                setItems(pair.getValue());
            }
        }
        else
        {
            setItems(null);
        }
    }

    public void setExtendedItems(ArrayList<Entry<VmTemplate, DiskImageList>> arrayList) {
        this.extendedItems = arrayList;
    }

    @Override
    protected void SyncSearch() {
    }
}
