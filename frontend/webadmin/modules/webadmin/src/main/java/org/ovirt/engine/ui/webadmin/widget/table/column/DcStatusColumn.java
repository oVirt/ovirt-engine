package org.ovirt.engine.ui.webadmin.widget.table.column;

import org.ovirt.engine.core.common.businessentities.StoragePool;

import com.google.gwt.resources.client.ImageResource;

public class DcStatusColumn extends AbstractWebAdminImageResourceColumn<StoragePool> {

    @Override
    public ImageResource getValue(StoragePool dc) {
        setEnumTitle(dc.getStatus());
        switch (dc.getStatus()) {
        case Contend:
            return getApplicationResources().waitImage();
        case Maintenance:
            return getApplicationResources().maintenanceImage();
        case NotOperational:
        case NonResponsive:
        case Uninitialized:
            return getApplicationResources().downImage();
        case Up:
            return getApplicationResources().upImage();

        default:
            break;
        }
        return null;
    }

}
