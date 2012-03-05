package org.ovirt.engine.ui.webadmin.widget.table.column;

import org.ovirt.engine.core.common.businessentities.storage_domains;

import com.google.gwt.resources.client.ImageResource;

public class StorageDomainSharedStatusColumn extends WebAdminImageResourceColumn<storage_domains> {

    @Override
    public ImageResource getValue(storage_domains sp) {
        switch (sp.getstorage_domain_shared_status()) {
        case Unattached:
            return getApplicationResources().tornChainImage();
        case Active:
            return getApplicationResources().upImage();
        case InActive:
            return getApplicationResources().downImage();
        case Mixed:
            return getApplicationResources().upalertImage();
        case Locked:
            return getApplicationResources().lockImage();
        default:
            return getApplicationResources().downImage();
        }
    }

}
