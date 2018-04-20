package org.ovirt.engine.ui.uicommonweb.models.templates;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.ovirt.engine.core.common.businessentities.VmTemplate;
import org.ovirt.engine.core.common.businessentities.comparators.DiskByDiskAliasComparator;
import org.ovirt.engine.core.common.businessentities.storage.DiskImage;
import org.ovirt.engine.ui.uicommonweb.models.SearchableListModel;
import org.ovirt.engine.ui.uicommonweb.models.vms.ImportTemplateData;

@SuppressWarnings("unused")
public class TemplateImportDiskListModel extends SearchableListModel {
    private List<Map.Entry<VmTemplate, List<DiskImage>>> extendedItems;

    public TemplateImportDiskListModel() {
        setIsTimerDisabled(true);
    }

    @Override
    protected void onEntityChanged() {
        super.onEntityChanged();

        if (getEntity() != null) {
            ArrayList<DiskImage> list = new ArrayList<>();
            VmTemplate template = (VmTemplate) getEntity();
            for (Map.Entry<VmTemplate, List<DiskImage>> item : extendedItems) {
                if (item.getKey().getQueryableId().equals(template.getQueryableId())) {
                    list.addAll(item.getValue());
                    Collections.sort(list, new DiskByDiskAliasComparator());
                    setItems(list);
                    return;
                }
            }
        } else {
            setItems(null);
        }
    }

    @Override
    public void setEntity(Object value) {
        super.setEntity(value != null ? ((ImportTemplateData) value).getTemplate() : null);
    }

    public void setExtendedItems(List<Map.Entry<VmTemplate, List<DiskImage>>> arrayList) {
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
