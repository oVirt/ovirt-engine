package org.ovirt.engine.ui.webadmin.widget.table.column;

import org.ovirt.engine.core.common.businessentities.gluster.StorageDevice;
import org.ovirt.engine.ui.common.widget.table.cell.AbstractCell;
import org.ovirt.engine.ui.webadmin.ApplicationResources;
import org.ovirt.engine.ui.webadmin.ApplicationTemplates;
import org.ovirt.engine.ui.webadmin.gin.AssetProvider;

import com.google.gwt.safehtml.shared.SafeHtml;
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;
import com.google.gwt.safehtml.shared.SafeHtmlUtils;
import com.google.gwt.user.client.ui.AbstractImagePrototype;

public class StorageDeviceStatusCell extends AbstractCell<StorageDevice> {

    private static final ApplicationTemplates templates = AssetProvider.getTemplates();
    private static final ApplicationResources resources = AssetProvider.getResources();

    @Override
    public void render(Context context, StorageDevice device, SafeHtmlBuilder sb, String id) {
        // No lock if we can create brick from the device
        if (device.getCanCreateBrick()) {
            return;
        }

        // Place a lock image to say that device is already in use and can't be used again
        SafeHtml statusImageHtml =
                SafeHtmlUtils.fromTrustedString(AbstractImagePrototype.create(resources.lockImage()).getHTML());
        sb.append(templates.statusTemplate(statusImageHtml, id, ""));
    }

}
