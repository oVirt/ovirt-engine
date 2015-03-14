package org.ovirt.engine.ui.common.widget.table.column;

import org.ovirt.engine.core.common.businessentities.storage.Disk;
import org.ovirt.engine.core.common.businessentities.storage.DiskImage;
import org.ovirt.engine.core.common.businessentities.storage.DiskStorageType;
import org.ovirt.engine.core.common.businessentities.storage.ImageStatus;
import org.ovirt.engine.ui.common.CommonApplicationConstants;
import org.ovirt.engine.ui.common.CommonApplicationResources;
import org.ovirt.engine.ui.common.gin.AssetProvider;
import org.ovirt.engine.ui.uicompat.EnumTranslator;

import com.google.gwt.resources.client.ImageResource;
import com.google.gwt.safehtml.shared.SafeHtml;
import com.google.gwt.safehtml.shared.SafeHtmlUtils;

public class DiskStatusColumn extends AbstractImageResourceColumn<Disk> {

    private final static CommonApplicationResources resources = AssetProvider.getResources();
    private final static CommonApplicationConstants constants = AssetProvider.getConstants();

    @Override
    public ImageResource getValue(Disk disk) {
        if (disk.getDiskStorageType().equals(DiskStorageType.IMAGE)) {
            DiskImage diskImage = (DiskImage) disk;

            if (diskImage.getImageStatus().equals(ImageStatus.LOCKED)) {
                return new DiskImageStatusColumn().getValue(diskImage);
            }
        }

        boolean isDiskPlugged = disk.getPlugged() != null && disk.getPlugged().booleanValue();
        return isDiskPlugged ? resources.upImage() : resources.downImage();
    }

    @Override
    public SafeHtml getTooltip(Disk disk) {
        String tooltipContent = null;
        if (disk.getDiskStorageType().equals(DiskStorageType.IMAGE)) {
            DiskImage diskImage = (DiskImage) disk;
            if (diskImage.getImageStatus().equals(ImageStatus.LOCKED)) {
                tooltipContent = EnumTranslator.getInstance().translate(diskImage.getImageStatus());
                return SafeHtmlUtils.fromString(tooltipContent);
            }
        }

        boolean isDiskPlugged = disk.getPlugged() != null && disk.getPlugged().booleanValue();
        tooltipContent = isDiskPlugged ? constants.active() : constants.inactive();
        return SafeHtmlUtils.fromString(tooltipContent);
    }
}
