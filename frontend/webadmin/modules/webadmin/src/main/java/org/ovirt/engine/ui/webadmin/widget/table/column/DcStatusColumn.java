package org.ovirt.engine.ui.webadmin.widget.table.column;

import org.ovirt.engine.core.common.businessentities.storage_pool;

import com.google.gwt.resources.client.ImageResource;

public class DcStatusColumn extends ImageResourceColumn<storage_pool> {

    @Override
    public ImageResource getValue(storage_pool dc) {
        switch (dc.getstatus()) {
        case Contend:
            return getApplicationResources().waitImage();
        case Maintanance:
            return getApplicationResources().maintenanceImage();
        case NotOperational:
        case Problematic:
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
