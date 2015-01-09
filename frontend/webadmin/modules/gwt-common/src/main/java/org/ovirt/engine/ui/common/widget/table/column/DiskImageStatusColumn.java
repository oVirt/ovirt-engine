package org.ovirt.engine.ui.common.widget.table.column;

import org.ovirt.engine.core.common.businessentities.DiskImage;

import com.google.gwt.resources.client.ImageResource;

public class DiskImageStatusColumn extends AbstractImageResourceColumn<DiskImage> {

    @Override
    public ImageResource getValue(DiskImage diskImage) {
        setEnumTitle(diskImage.getImageStatus());

        switch (diskImage.getImageStatus()) {
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
