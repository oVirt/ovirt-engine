package org.ovirt.engine.ui.uicommonweb.models.templates;

import org.ovirt.engine.core.common.businessentities.VmTemplate;
import org.ovirt.engine.ui.uicommonweb.models.SearchableListModel;
import org.ovirt.engine.ui.uicommonweb.models.vms.ImportTemplateData;

@SuppressWarnings("unused")
public class TemplateImportInterfaceListModel extends SearchableListModel {
    public TemplateImportInterfaceListModel() {
        setIsTimerDisabled(true);
    }

    @Override
    protected void onEntityChanged() {
        super.onEntityChanged();

        if (getEntity() != null) {
            VmTemplate template = (VmTemplate) getEntity();
            setItems(template.getInterfaces());
        } else {
            setItems(null);
        }
    }

    @Override
    public void setEntity(Object value) {
        super.setEntity(value != null ? ((ImportTemplateData) value).getTemplate() : null);
    }

    @Override
    protected void syncSearch() {
    }

    @Override
    protected String getListName() {
        return "TemplateImportInterfaceListModel"; //$NON-NLS-1$
    }
}
