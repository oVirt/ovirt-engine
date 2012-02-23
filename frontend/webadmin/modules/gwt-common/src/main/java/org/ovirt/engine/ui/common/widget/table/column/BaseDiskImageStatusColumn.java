package org.ovirt.engine.ui.common.widget.table.column;

import org.ovirt.engine.core.common.businessentities.DiskImage;
import org.ovirt.engine.ui.common.CommonApplicationResources;

import com.google.gwt.cell.client.Cell;
import com.google.gwt.resources.client.ImageResource;

public class BaseDiskImageStatusColumn extends BaseImageResourceColumn<DiskImage> {

    private final CommonApplicationResources resources;

    public BaseDiskImageStatusColumn(Cell<ImageResource> cell, CommonApplicationResources resources) {
        super(cell);
        this.resources = resources;
    }

    @Override
    public ImageResource getValue(DiskImage object) {
        return (object.getPlugged() != null && object.getPlugged().booleanValue()) ?
                resources.upImage() : resources.downImage();
    }

}
