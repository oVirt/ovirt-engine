package org.ovirt.engine.ui.webadmin.widget.table.column;

import org.ovirt.engine.core.common.businessentities.StorageDomain;
import org.ovirt.engine.core.common.businessentities.StorageType;

import com.google.gwt.resources.client.ImageResource;

public class StorageDomainStatusColumn extends AbstractWebAdminImageResourceColumn<StorageDomain> {

    @Override
    public ImageResource getValue(StorageDomain sp) {
        setEnumTitle(sp.getStatus());
        switch (sp.getStatus()) {
        case Unattached:
            if (sp.getStorageType() == StorageType.GLANCE) {
                return getApplicationResources().openstackImage();
            } else {
                return getApplicationResources().tornChainImage();
            }
        case Active:
            return getApplicationResources().upImage();
        case Inactive:
            return getApplicationResources().downImage();
        case Uninitialized:
            return getApplicationResources().unconfiguredImage();
        case Activating:
        case Locked:
        case PreparingForMaintenance:
        case Detaching:
            return getApplicationResources().lockImage();
        case Maintenance:
            return getApplicationResources().maintenanceImage();
        default:
            return getApplicationResources().downImage();
        }
    }

}
