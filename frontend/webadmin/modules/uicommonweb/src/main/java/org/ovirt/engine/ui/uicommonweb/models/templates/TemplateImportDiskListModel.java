package org.ovirt.engine.ui.uicommonweb.models.templates;

import java.util.ArrayList;
import java.util.Map;

import org.ovirt.engine.core.common.businessentities.DiskImage;
import org.ovirt.engine.core.common.businessentities.VmTemplate;
import org.ovirt.engine.core.common.queries.DiskImageList;
import org.ovirt.engine.ui.uicommonweb.Linq;
import org.ovirt.engine.ui.uicommonweb.Linq.DiskByAliasComparer;
import org.ovirt.engine.ui.uicommonweb.models.SearchableListModel;
import org.ovirt.engine.ui.uicommonweb.models.vms.ImportTemplateData;

@SuppressWarnings("unused")
public class TemplateImportDiskListModel extends SearchableListModel
{
    private ArrayList<Map.Entry<VmTemplate, DiskImageList>> extendedItems;

    public TemplateImportDiskListModel() {
        setIsTimerDisabled(true);
    }

    @Override
    protected void onEntityChanged()
    {
        super.onEntityChanged();

        if (getEntity() != null)
        {
            ArrayList<DiskImage> list = new ArrayList<DiskImage>();
            VmTemplate template = ((ImportTemplateData) getEntity()).getTemplate();
            for (Map.Entry<VmTemplate, DiskImageList> item : extendedItems) {
                if (item.getKey().getQueryableId().equals(template.getQueryableId())) {
                    DiskImageList diskImageList = item.getValue();
                    for (DiskImage diskImage : diskImageList.getDiskImages()) {
                        list.add(diskImage);
                    }
                    Linq.sort(list, new DiskByAliasComparer());
                    setItems(list);
                    return;
                }
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
    protected void syncSearch() {
    }

    @Override
    protected String getListName() {
        return "TemplateImportDiskListModel"; //$NON-NLS-1$
    }
}
