package org.ovirt.engine.ui.common.widget.table.column;

import org.ovirt.engine.core.common.businessentities.storage.DiskImage;
import org.ovirt.engine.ui.common.CommonApplicationResources;
import org.ovirt.engine.ui.common.gin.AssetProvider;

import com.google.gwt.resources.client.ImageResource;

public class DiskImageStatusColumn extends AbstractImageResourceColumn<DiskImage> {

    private final static CommonApplicationResources resources = AssetProvider.getResources();

    @Override
    public ImageResource getValue(DiskImage diskImage) {
        setEnumTitle(diskImage.getImageStatus());

        switch (diskImage.getImageStatus()) {
        case OK:
            return resources.upImage();
        case LOCKED:
            return resources.waitImage();
        case ILLEGAL:
            return resources.logErrorImage();
        default:
            return null;
        }
    }

}
