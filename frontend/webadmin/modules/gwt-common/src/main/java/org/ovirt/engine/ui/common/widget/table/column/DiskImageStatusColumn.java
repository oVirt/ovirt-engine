package org.ovirt.engine.ui.common.widget.table.column;

import org.ovirt.engine.core.common.businessentities.DiskImage;

import com.google.gwt.resources.client.ImageResource;

public class DiskImageStatusColumn extends ImageResourceColumn<DiskImage> {

    @Override
    public ImageResource getValue(DiskImage diskImage) {
        switch (diskImage.getimageStatus()) {
        case OK:
            return getCommonResources().upImage();
        case LOCKED:
            return getCommonResources().waitImage();
        case ILLEGAL:
            return getCommonResources().logErrorImage();
        default:
            return null;
        }
    }

}
