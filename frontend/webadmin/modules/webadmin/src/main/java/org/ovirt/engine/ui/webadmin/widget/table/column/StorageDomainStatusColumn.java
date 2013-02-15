package org.ovirt.engine.ui.webadmin.widget.table.column;

import org.ovirt.engine.core.common.businessentities.StorageDomain;

import com.google.gwt.resources.client.ImageResource;

public class StorageDomainStatusColumn extends WebAdminImageResourceColumn<StorageDomain> {

    @Override
    public ImageResource getValue(StorageDomain sp) {
        setEnumTitle(sp.getStatus());
        switch (sp.getStatus()) {
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
