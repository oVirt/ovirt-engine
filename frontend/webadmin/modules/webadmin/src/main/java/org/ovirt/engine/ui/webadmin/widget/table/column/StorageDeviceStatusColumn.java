package org.ovirt.engine.ui.webadmin.widget.table.column;

import org.ovirt.engine.core.common.businessentities.gluster.StorageDevice;
import org.ovirt.engine.ui.common.widget.table.column.AbstractImageResourceColumn;
import org.ovirt.engine.ui.uicommonweb.Linq;
import org.ovirt.engine.ui.webadmin.ApplicationConstants;
import org.ovirt.engine.ui.webadmin.ApplicationResources;
import org.ovirt.engine.ui.webadmin.gin.AssetProvider;

import com.google.gwt.resources.client.ImageResource;
import com.google.gwt.safehtml.shared.SafeHtml;
import com.google.gwt.safehtml.shared.SafeHtmlUtils;

public class StorageDeviceStatusColumn extends AbstractImageResourceColumn<StorageDevice> {

    private static final ApplicationResources resources = AssetProvider.getResources();
    private static final ApplicationConstants constants = AssetProvider.getConstants();

    @Override
    public ImageResource getValue(StorageDevice device) {
        // No lock if we can create brick from the device
        if (device.getCanCreateBrick()) {
            return null;
        }
        return resources.lockImage();
    }

    public void makeSortable() {
        makeSortable(Linq.StorageDeviceComparer);
    }

    @Override
    public SafeHtml getTooltip(StorageDevice device) {
        if (device.getCanCreateBrick()) {
            return null;
        }
        return SafeHtmlUtils.fromSafeConstant(constants.deviceIsAlreadyUsed());
    }
}
