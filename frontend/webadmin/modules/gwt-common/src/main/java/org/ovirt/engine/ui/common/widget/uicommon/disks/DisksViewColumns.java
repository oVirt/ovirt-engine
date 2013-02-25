package org.ovirt.engine.ui.common.widget.uicommon.disks;

import java.util.Date;

import com.google.gwt.i18n.client.DateTimeFormat;

import org.ovirt.engine.core.common.businessentities.Disk;
import org.ovirt.engine.core.common.businessentities.Disk.DiskStorageType;
import org.ovirt.engine.core.common.businessentities.DiskImage;
import org.ovirt.engine.core.common.businessentities.DiskInterface;
import org.ovirt.engine.core.common.businessentities.ImageStatus;
import org.ovirt.engine.core.common.businessentities.LunDisk;
import org.ovirt.engine.core.common.businessentities.VmEntityType;
import org.ovirt.engine.core.common.businessentities.VolumeType;
import org.ovirt.engine.ui.common.CommonApplicationConstants;
import org.ovirt.engine.ui.common.CommonApplicationMessages;
import org.ovirt.engine.ui.common.CommonApplicationResources;
import org.ovirt.engine.ui.common.widget.renderer.DiskSizeRenderer.DiskSizeUnit;
import org.ovirt.engine.ui.common.widget.table.column.DiskContainersColumn;
import org.ovirt.engine.ui.common.widget.table.column.DiskSizeColumn;
import org.ovirt.engine.ui.common.widget.table.column.DiskStatusColumn;
import org.ovirt.engine.ui.common.widget.table.column.EnumColumn;
import org.ovirt.engine.ui.common.widget.table.column.FullDateTimeColumn;
import org.ovirt.engine.ui.common.widget.table.column.ImageResourceColumn;
import org.ovirt.engine.ui.common.widget.table.column.StorageDomainsColumn;
import org.ovirt.engine.ui.common.widget.table.column.TextColumnWithTooltip;


import com.google.gwt.core.client.GWT;
import com.google.gwt.resources.client.ImageResource;

public class DisksViewColumns {
    private static final CommonApplicationResources resources = GWT.create(CommonApplicationResources.class);
    private static final CommonApplicationConstants constants = GWT.create(CommonApplicationConstants.class);
    private static final CommonApplicationMessages messages = GWT.create(CommonApplicationMessages.class);

    public static final TextColumnWithTooltip<Disk> aliasColumn = new TextColumnWithTooltip<Disk>() {
        @Override
        public String getValue(Disk object) {
            return object.getDiskAlias();
        }
    };

    public static final TextColumnWithTooltip<Disk> idColumn = new TextColumnWithTooltip<Disk>() {
        @Override
        public String getValue(Disk object) {
            return object.getId().toString();
        }
    };

    public static final ImageResourceColumn<Disk> bootableDiskColumn = new ImageResourceColumn<Disk>() {
        @Override
        public ImageResource getValue(Disk object) {
            setTitle(object.isBoot() ? getDefaultTitle() : null);
            return object.isBoot() ? getDefaultImage() : null;
        }

        @Override
        public String getDefaultTitle() {
            return constants.bootableDisk();
        }

        @Override
        public ImageResource getDefaultImage() {
            return resources.bootableDiskIcon();
        }
    };

    public static final ImageResourceColumn<Disk> shareableDiskColumn = new ImageResourceColumn<Disk>() {
        @Override
        public ImageResource getValue(Disk object) {
            setTitle(object.isShareable() ? getDefaultTitle() : null);
            return object.isShareable() ? getDefaultImage() : null;
        }

        @Override
        public String getDefaultTitle() {
            return constants.shareable();
        }

        @Override
        public ImageResource getDefaultImage() {
            return resources.shareableDiskIcon();
        }
    };

    public static final ImageResourceColumn<Disk> lunDiskColumn = new ImageResourceColumn<Disk>() {
        @Override
        public ImageResource getValue(Disk object) {
            setTitle(object.getDiskStorageType() == DiskStorageType.LUN ? getDefaultTitle() : null);
            return object.getDiskStorageType() == DiskStorageType.LUN ?
                    resources.externalDiskIcon() : null;
        }

        @Override
        public String getDefaultTitle() {
            return constants.lunDisksLabel();
        }

        @Override
        public ImageResource getDefaultImage() {
            return resources.externalDiskIcon();
        }
    };

    public static final ImageResourceColumn<Disk> diskContainersIconColumn = new ImageResourceColumn<Disk>() {
        @Override
        public ImageResource getValue(Disk object) {
            setEnumTitle(object.getVmEntityType());
            return object.getVmEntityType() == VmEntityType.VM ? resources.vmsImage() :
                    object.getVmEntityType() == VmEntityType.TEMPLATE ? resources.templatesImage() : null;
        }
    };

    public static final DiskStatusColumn diskStatusColumn = new DiskStatusColumn();

    public static final DiskContainersColumn diskContainersColumn = new DiskContainersColumn();

    public static final TextColumnWithTooltip<Disk> diskAlignmentColumn = new TextColumnWithTooltip<Disk>() {
        @Override
        public String getValue(Disk object) {
            if (object.getLastAlignmentScan() != null) {
                String lastScanDate = DateTimeFormat
                        .getFormat("yyyy-MM-dd, HH:mm").format(object.getLastAlignmentScan()); //$NON-NLS-1$
                setTitle(messages.lastDiskAlignment(lastScanDate));
            } else {
                setTitle(null);
            }
            return object.getAlignment().toString();
        }
    };

    public static final StorageDomainsColumn storageDomainsColumn = new StorageDomainsColumn();

    public static final DiskSizeColumn<Disk> sizeColumn = new DiskSizeColumn<Disk>() {
        @Override
        protected Long getRawValue(Disk object) {
            return object.getDiskStorageType() == DiskStorageType.IMAGE ?
                    ((DiskImage) object).getSize() :
                    (long) (((LunDisk) object).getLun().getDeviceSize() * Math.pow(1024, 3));
        }
    };

    public static final DiskSizeColumn<Disk> actualSizeColumn = new DiskSizeColumn<Disk>(DiskSizeUnit.GIGABYTE) {
        @Override
        protected Long getRawValue(Disk object) {
            return object.getDiskStorageType() == DiskStorageType.IMAGE ?
                    Math.round(((DiskImage) object).getActualDiskWithSnapshotsSize())
                    : (long) (((LunDisk) object).getLun().getDeviceSize());
        }
    };

    public static final TextColumnWithTooltip<Disk> allocationColumn = new EnumColumn<Disk, VolumeType>() {
        @Override
        protected VolumeType getRawValue(Disk object) {
            return object.getDiskStorageType() == DiskStorageType.IMAGE ?
                    ((DiskImage) object).getVolumeType() : null;
        }
    };

    public static final TextColumnWithTooltip<Disk> interfaceColumn = new EnumColumn<Disk, DiskInterface>() {
        @Override
        protected DiskInterface getRawValue(Disk object) {
            return object.getDiskInterface();
        }
    };

    public static final TextColumnWithTooltip<Disk> dateCreatedColumn = new FullDateTimeColumn<Disk>() {
        @Override
        protected Date getRawValue(Disk object) {
            return object.getDiskStorageType() == DiskStorageType.IMAGE ?
                    ((DiskImage) object).getCreationDate() : null;
        }
    };

    public static final TextColumnWithTooltip<Disk> statusColumn = new EnumColumn<Disk, ImageStatus>() {
        @Override
        protected ImageStatus getRawValue(Disk object) {
            return object.getDiskStorageType() == DiskStorageType.IMAGE ?
                    ((DiskImage) object).getImageStatus() : null;
        }
    };

    public static final TextColumnWithTooltip<Disk> descriptionColumn = new TextColumnWithTooltip<Disk>() {
        @Override
        public String getValue(Disk object) {
            return object.getDiskDescription();
        }
    };

    public static final TextColumnWithTooltip<Disk> lunIdColumn = new TextColumnWithTooltip<Disk>() {
        @Override
        public String getValue(Disk object) {
            return object.getDiskStorageType() == DiskStorageType.LUN ?
                    ((LunDisk) object).getLun().getLUN_id() : null;
        }
    };

    public static final TextColumnWithTooltip<Disk> lunVendorIdColumn = new TextColumnWithTooltip<Disk>() {
        @Override
        public String getValue(Disk object) {
            return object.getDiskStorageType() == DiskStorageType.LUN ?
                    ((LunDisk) object).getLun().getVendorId() : null;
        }
    };

    public static final TextColumnWithTooltip<Disk> lunProductIdColumn = new TextColumnWithTooltip<Disk>() {
        @Override
        public String getValue(Disk object) {
            return object.getDiskStorageType() == DiskStorageType.LUN ?
                    ((LunDisk) object).getLun().getProductId() : null;
        }
    };

    public static final TextColumnWithTooltip<Disk> lunSerialColumn = new TextColumnWithTooltip<Disk>() {
        @Override
        public String getValue(Disk object) {
            return object.getDiskStorageType() == DiskStorageType.LUN ?
                    ((LunDisk) object).getLun().getSerial() : null;
        }
    };
}
