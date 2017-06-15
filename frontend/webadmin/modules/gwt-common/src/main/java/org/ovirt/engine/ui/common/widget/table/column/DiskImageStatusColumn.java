package org.ovirt.engine.ui.common.widget.table.column;

import org.ovirt.engine.core.common.businessentities.storage.DiskImage;
import org.ovirt.engine.ui.common.CommonApplicationResources;
import org.ovirt.engine.ui.common.gin.AssetProvider;
import org.ovirt.engine.ui.uicompat.EnumTranslator;

import com.google.gwt.resources.client.ImageResource;
import com.google.gwt.safehtml.shared.SafeHtml;
import com.google.gwt.safehtml.shared.SafeHtmlUtils;

public class DiskImageStatusColumn extends AbstractImageResourceColumn<DiskImage> {

    private static final CommonApplicationResources resources = AssetProvider.getResources();

    @Override
    public ImageResource getValue(DiskImage diskImage) {

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

    @Override
    public SafeHtml getTooltip(DiskImage diskImage) {
        String status = EnumTranslator.getInstance().translate(diskImage.getImageStatus());
        return SafeHtmlUtils.fromString(status);
    }

}
