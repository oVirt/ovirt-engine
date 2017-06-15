package org.ovirt.engine.ui.webadmin.widget.table.column;

import org.ovirt.engine.core.common.businessentities.StoragePool;
import org.ovirt.engine.ui.common.widget.table.column.AbstractImageResourceColumn;
import org.ovirt.engine.ui.uicompat.EnumTranslator;
import org.ovirt.engine.ui.webadmin.ApplicationResources;
import org.ovirt.engine.ui.webadmin.gin.AssetProvider;

import com.google.gwt.resources.client.ImageResource;
import com.google.gwt.safehtml.shared.SafeHtml;
import com.google.gwt.safehtml.shared.SafeHtmlUtils;

public class DcStatusColumn extends AbstractImageResourceColumn<StoragePool> {

    private static final ApplicationResources resources = AssetProvider.getResources();

    @Override
    public ImageResource getValue(StoragePool dc) {
        switch (dc.getStatus()) {
        case Contend:
            return resources.waitImage();
        case Maintenance:
            return resources.maintenanceImage();
        case NotOperational:
        case NonResponsive:
        case Uninitialized:
            return resources.downImage();
        case Up:
            return resources.upImage();

        default:
            break;
        }
        return null;
    }

    @Override
    public SafeHtml getTooltip(StoragePool dc) {
        String tooltipContent = EnumTranslator.getInstance().translate(dc.getStatus());
        return SafeHtmlUtils.fromString(tooltipContent);
    }

}
