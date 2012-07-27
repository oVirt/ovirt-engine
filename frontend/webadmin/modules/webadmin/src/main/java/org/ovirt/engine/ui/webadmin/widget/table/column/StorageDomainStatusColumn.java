package org.ovirt.engine.ui.webadmin.widget.table.column;

import org.ovirt.engine.core.common.businessentities.storage_domains;

import com.google.gwt.resources.client.ImageResource;

public class StorageDomainStatusColumn extends WebAdminImageResourceColumn<storage_domains> {

    @Override
    public ImageResource getValue(storage_domains sp) {
        setEnumTitle(sp.getstatus());
        switch (sp.getstatus()) {
        case Unattached:
            return getApplicationResources().tornChainImage();
        case Active:
            return getApplicationResources().upImage();
        case InActive:
            return getApplicationResources().downImage();
        case Uninitialized:
            return getApplicationResources().unconfiguredImage();
        case Locked:
            return getApplicationResources().lockImage();
        default:
            return getApplicationResources().downImage();
        }
    }

}
