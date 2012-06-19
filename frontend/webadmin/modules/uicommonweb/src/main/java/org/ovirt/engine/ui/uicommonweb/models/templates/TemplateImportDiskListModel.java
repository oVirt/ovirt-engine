package org.ovirt.engine.ui.uicommonweb.models.templates;

import java.util.ArrayList;
import java.util.Map;

import org.ovirt.engine.core.common.businessentities.DiskImage;
import org.ovirt.engine.core.common.businessentities.VmTemplate;
import org.ovirt.engine.core.common.queries.DiskImageList;
import org.ovirt.engine.ui.uicommonweb.Linq;
import org.ovirt.engine.ui.uicommonweb.Linq.DiskByAliasComparer;

@SuppressWarnings("unused")
public class TemplateImportDiskListModel extends TemplateDiskListModel
{
    private ArrayList<Map.Entry<VmTemplate, DiskImageList>> extendedItems;

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
                for (Map.Entry<VmTemplate, DiskImageList> item : extendedItems) {
                    if (item.getKey().getQueryableId().equals(template.getQueryableId())) {
                        DiskImageList diskImageList = item.getValue();
                        for (DiskImage diskImage : diskImageList.getDiskImages()) {
                            list.add(diskImage);
                        }
                        Linq.Sort(list, new DiskByAliasComparer());
                        setItems(list);
                        return;
                    }
                }
            }
            else
            {
                Map.Entry<VmTemplate, ArrayList<DiskImage>> pair =
                        (Map.Entry<VmTemplate, ArrayList<DiskImage>>) getEntity();
                setItems(pair.getValue());
            }
        }
        else
        {
            setItems(null);
        }
    }

    public void setExtendedItems(ArrayList<Map.Entry<VmTemplate, DiskImageList>> arrayList) {
        this.extendedItems = arrayList;
    }

    @Override
    protected void SyncSearch() {
    }
}
