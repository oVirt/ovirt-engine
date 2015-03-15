package org.ovirt.engine.ui.webadmin.widget.table.column;

import org.ovirt.engine.core.common.businessentities.StorageDomain;
import org.ovirt.engine.core.common.businessentities.StorageDomainType;
import org.ovirt.engine.core.common.businessentities.storage.StorageType;

import com.google.gwt.resources.client.ImageResource;

public class StorageDomainSharedStatusColumn extends AbstractWebAdminImageResourceColumn<StorageDomain> {

    @Override
    public ImageResource getValue(StorageDomain sp) {
        if (sp.getStorageDomainType() == StorageDomainType.ISO) {
            setEnumTitle(sp.getStorageDomainSharedStatus());
            switch (sp.getStorageDomainSharedStatus()) {
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
                case Mixed:
                    return getApplicationResources().upalertImage();
                case Locked:
                    return getApplicationResources().lockImage();
                default:
                    return getApplicationResources().downImage();
            }
        }
        else {
            return new StorageDomainStatusColumn().getValue(sp);
        }
    }
}
