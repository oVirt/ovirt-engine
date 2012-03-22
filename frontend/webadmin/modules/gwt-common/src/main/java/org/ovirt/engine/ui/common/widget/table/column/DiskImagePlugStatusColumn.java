package org.ovirt.engine.ui.common.widget.table.column;

import org.ovirt.engine.core.common.businessentities.DiskImage;

import com.google.gwt.resources.client.ImageResource;

public class DiskImagePlugStatusColumn extends ImageResourceColumn<DiskImage> {

    @Override
    public ImageResource getValue(DiskImage object) {
        return (object.getPlugged() != null && object.getPlugged().booleanValue()) ?
                getCommonResources().upImage() : getCommonResources().downImage();
    }

}
