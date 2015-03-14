package org.ovirt.engine.ui.webadmin.widget.table.column;

import org.ovirt.engine.core.common.businessentities.StorageDomain;
import org.ovirt.engine.core.common.businessentities.StorageDomainType;
import org.ovirt.engine.core.common.businessentities.storage.StorageType;
import org.ovirt.engine.ui.common.widget.table.column.AbstractImageResourceColumn;
import org.ovirt.engine.ui.webadmin.ApplicationResources;
import org.ovirt.engine.ui.webadmin.gin.AssetProvider;

import com.google.gwt.resources.client.ImageResource;

public class StorageDomainSharedStatusColumn extends AbstractImageResourceColumn<StorageDomain> {

    private final static ApplicationResources resources = AssetProvider.getResources();

    @Override
    public ImageResource getValue(StorageDomain sp) {
        if (sp.getStorageDomainType() == StorageDomainType.ISO) {
            setEnumTitle(sp.getStorageDomainSharedStatus());
            switch (sp.getStorageDomainSharedStatus()) {
                case Unattached:
                    if (sp.getStorageType() == StorageType.GLANCE) {
                        return resources.openstackImage();
                    } else {
                        return resources.tornChainImage();
                    }
                case Active:
                    return resources.upImage();
                case Inactive:
                    return resources.downImage();
                case Mixed:
                    return resources.upalertImage();
                case Locked:
                    return resources.lockImage();
                default:
                    return resources.downImage();
            }
        }
        else {
            return new StorageDomainStatusColumn().getValue(sp);
        }
    }
}
