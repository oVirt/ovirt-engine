package org.ovirt.engine.ui.uicommonweb.models.templates;


import java.util.Collection;

public class UserPortalTemplateDiskListModel extends TemplateDiskListModel {

    @Override
    public void setItems(Collection value) {
        ignoreStorageDomains = true;
        super.setItems(value);
    }
}
