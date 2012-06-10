package org.ovirt.engine.ui.uicommonweb.models.templates;


public class UserPortalTemplateDiskListModel extends TemplateDiskListModel {

    @Override
    public void setItems(Iterable value) {
        ignoreStorageDomains = true;
        super.setItems(value);
    }
}
