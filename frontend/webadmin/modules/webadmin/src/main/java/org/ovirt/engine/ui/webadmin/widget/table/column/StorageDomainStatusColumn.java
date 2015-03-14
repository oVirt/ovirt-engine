package org.ovirt.engine.ui.webadmin.widget.table.column;

import org.ovirt.engine.core.common.businessentities.StorageDomain;
import org.ovirt.engine.core.common.businessentities.storage.StorageType;
import org.ovirt.engine.ui.common.widget.table.column.AbstractImageResourceColumn;
import org.ovirt.engine.ui.webadmin.ApplicationResources;
import org.ovirt.engine.ui.webadmin.gin.AssetProvider;

import com.google.gwt.resources.client.ImageResource;

public class StorageDomainStatusColumn extends AbstractImageResourceColumn<StorageDomain> {

    private final static ApplicationResources resources = AssetProvider.getResources();

    @Override
    public ImageResource getValue(StorageDomain sp) {
        setEnumTitle(sp.getStatus());
        switch (sp.getStatus()) {
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
        case Uninitialized:
            return resources.unconfiguredImage();
        case Activating:
        case Locked:
        case PreparingForMaintenance:
        case Detaching:
            return resources.lockImage();
        case Maintenance:
            return resources.maintenanceImage();
        default:
            return resources.downImage();
        }
    }

}
