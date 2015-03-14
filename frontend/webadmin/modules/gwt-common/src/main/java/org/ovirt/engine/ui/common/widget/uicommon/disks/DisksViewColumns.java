package org.ovirt.engine.ui.common.widget.uicommon.disks;

import java.util.ArrayList;
import java.util.Date;

import org.ovirt.engine.core.common.businessentities.storage.Disk;
import org.ovirt.engine.core.common.businessentities.storage.DiskImage;
import org.ovirt.engine.core.common.businessentities.storage.DiskInterface;
import org.ovirt.engine.core.common.businessentities.storage.DiskStorageType;
import org.ovirt.engine.core.common.businessentities.storage.ImageStatus;
import org.ovirt.engine.core.common.businessentities.storage.LunDisk;
import org.ovirt.engine.core.common.businessentities.storage.StorageType;
import org.ovirt.engine.core.common.businessentities.storage.VolumeType;
import org.ovirt.engine.core.common.utils.SizeConverter;
import org.ovirt.engine.core.compat.StringHelper;
import org.ovirt.engine.ui.common.CommonApplicationConstants;
import org.ovirt.engine.ui.common.CommonApplicationMessages;
import org.ovirt.engine.ui.common.CommonApplicationResources;
import org.ovirt.engine.ui.common.gin.AssetProvider;
import org.ovirt.engine.ui.common.widget.table.column.AbstractCheckboxColumn;
import org.ovirt.engine.ui.common.widget.table.column.AbstractDiskSizeColumn;
import org.ovirt.engine.ui.common.widget.table.column.AbstractEnumColumn;
import org.ovirt.engine.ui.common.widget.table.column.AbstractFullDateTimeColumn;
import org.ovirt.engine.ui.common.widget.table.column.AbstractImageResourceColumn;
import org.ovirt.engine.ui.common.widget.table.column.AbstractTextColumn;
import org.ovirt.engine.ui.common.widget.table.column.DiskContainersColumn;
import org.ovirt.engine.ui.common.widget.table.column.DiskStatusColumn;
import org.ovirt.engine.ui.common.widget.table.column.StorageDomainsColumn;
import org.ovirt.engine.ui.uicommonweb.models.EntityModel;
import org.ovirt.engine.ui.uicommonweb.models.vms.DiskModel;
import org.ovirt.engine.ui.uicompat.EnumTranslator;

import com.google.gwt.cell.client.FieldUpdater;
import com.google.gwt.i18n.client.DateTimeFormat;
import com.google.gwt.resources.client.ImageResource;
import com.google.gwt.safehtml.shared.SafeHtml;
import com.google.gwt.safehtml.shared.SafeHtmlUtils;

public class DisksViewColumns {

    private final static CommonApplicationResources resources = AssetProvider.getResources();
    private final static CommonApplicationConstants constants = AssetProvider.getConstants();
    private final static CommonApplicationMessages messages = AssetProvider.getMessages();

    public static AbstractTextColumn<Disk> getAliasColumn(String sortBy) {
        AbstractTextColumn<Disk> column = new AbstractTextColumn<Disk>() {
            @Override
            public String getValue(Disk object) {
                return object.getDiskAlias();
            }
        };

        return makeSortable(column, sortBy);
    }

    public static AbstractTextColumn<Disk> getIdColumn(String sortBy) {
        AbstractTextColumn<Disk> column = new AbstractTextColumn<Disk>() {
            @Override
            public String getValue(Disk object) {
                return object.getId().toString();
            }
        };

        return makeSortable(column, sortBy);
    }

    public static AbstractTextColumn<Disk> getQoutaColumn(String sortBy) {
        AbstractTextColumn<Disk> column = new AbstractTextColumn<Disk>() {
            @Override
            public String getValue(Disk object) {
                String value = "";
                if (object.getDiskStorageType() == DiskStorageType.IMAGE) {
                    DiskImage diskImage = (DiskImage) object;
                    ArrayList<String> quotaNamesArr = diskImage.getQuotaNames();
                    if (quotaNamesArr != null) {
                        value = StringHelper.join(", ", quotaNamesArr.toArray());//$NON-NLS-1$
                    }
                }
                return value;
            }
        };

        return makeSortable(column, sortBy);
    }

    public static final AbstractImageResourceColumn<Disk> bootableDiskColumn = new AbstractImageResourceColumn<Disk>() {
        @Override
        public ImageResource getValue(Disk object) {
            return object.isBoot() ? getDefaultImage() : null;
        }

        @Override
        public ImageResource getDefaultImage() {
            return resources.bootableDiskIcon();
        }

        @Override
        public SafeHtml getTooltip(Disk object) {
            if (object.isBoot()) {
                return SafeHtmlUtils.fromSafeConstant(constants.bootableDisk());
            }
            return null;
        }
    };

    public static final AbstractImageResourceColumn<Disk> shareableDiskColumn = new AbstractImageResourceColumn<Disk>() {
        @Override
        public ImageResource getValue(Disk object) {
            return object.isShareable() ? getDefaultImage() : null;
        }

        @Override
        public ImageResource getDefaultImage() {
            return resources.shareableDiskIcon();
        }

        @Override
        public SafeHtml getTooltip(Disk object) {
            if (object.isShareable()) {
                return SafeHtmlUtils.fromSafeConstant(constants.shareable());
            }
            return null;
        }
    };

    public static final AbstractImageResourceColumn<Disk> readOnlyDiskColumn = new AbstractImageResourceColumn<Disk>() {
        @Override
        public ImageResource getValue(Disk object) {
            return object.getReadOnly() ? getDefaultImage() : null;
        }

        @Override
        public ImageResource getDefaultImage() {
            return resources.readOnlyDiskIcon();
        }

        @Override
        public SafeHtml getTooltip(Disk object) {
            if (object.getReadOnly()) {
                return SafeHtmlUtils.fromSafeConstant(constants.readOnly());
            }
            return null;
        }
    };

    public static final AbstractImageResourceColumn<Disk> lunDiskColumn = new AbstractImageResourceColumn<Disk>() {
        @Override
        public ImageResource getValue(Disk object) {
            return object.getDiskStorageType() == DiskStorageType.LUN ?
                    resources.externalDiskIcon() : null;
        }

        @Override
        public ImageResource getDefaultImage() {
            return resources.externalDiskIcon();
        }

        @Override
        public SafeHtml getTooltip(Disk object) {
            if (object.getDiskStorageType() == DiskStorageType.LUN) {
                return SafeHtmlUtils.fromSafeConstant(constants.lunDisksLabel());
            }
            return null;
        }
    };

    public static final AbstractImageResourceColumn<Disk> diskContainersIconColumn = new AbstractImageResourceColumn<Disk>() {
        @Override
        public ImageResource getValue(Disk object) {
            if (object.getVmEntityType() == null) {
                return null;
            }
            return object.getVmEntityType().isVmType() ? resources.vmsImage() :
                    object.getVmEntityType().isTemplateType() ? resources.templatesImage() : null;
        }

        @Override
        public SafeHtml getTooltip(Disk object) {
            if (object.getVmEntityType() == null) {
                return SafeHtmlUtils.fromSafeConstant(constants.unattachedDisk());
            }
            else {
                String status = EnumTranslator.getInstance().translate(object.getVmEntityType());
                return SafeHtmlUtils.fromString(status);
            }
        }
    };

    public static final DiskStatusColumn diskStatusColumn = new DiskStatusColumn();

    public static final DiskContainersColumn diskContainersColumn = new DiskContainersColumn();

    public static final AbstractTextColumn<Disk> diskAlignmentColumn = new AbstractTextColumn<Disk>() {
        @Override
        public String getValue(Disk object) {
            return object.getAlignment().toString();
        }

        @Override
        public SafeHtml getTooltip(Disk object) {
            if (object.getLastAlignmentScan() != null) {
                String lastScanDate = DateTimeFormat
                        .getFormat("yyyy-MM-dd, HH:mm").format(object.getLastAlignmentScan()); //$NON-NLS-1$
                return SafeHtmlUtils.fromString(messages.lastDiskAlignment(lastScanDate));
            }
            return null;
        }
    };

    public static final StorageDomainsColumn storageDomainsColumn = new StorageDomainsColumn();

    public static final AbstractTextColumn<Disk> storageTypeColumn = new AbstractEnumColumn<Disk, StorageType>() {
        @Override
        protected StorageType getRawValue(Disk object) {
            if (object.getDiskStorageType() != DiskStorageType.IMAGE) {
                return null;
            }
            DiskImage disk = (DiskImage) object;

            return disk.getStorageTypes().isEmpty() ? null : disk.getStorageTypes().get(0);
        }
    };

    public static final AbstractDiskSizeColumn<Disk> getSizeColumn(String sortBy) {
        AbstractDiskSizeColumn<Disk> column = new AbstractDiskSizeColumn<Disk>() {
            @Override
            protected Long getRawValue(Disk object) {
                return object.getDiskStorageType() == DiskStorageType.IMAGE ?
                        ((DiskImage) object).getSize() :
                        (long) (((LunDisk) object).getLun().getDeviceSize() * Math.pow(1024, 3));
            }
        };

        return makeSortable(column, sortBy);
    }

    public static final AbstractDiskSizeColumn<Disk> getActualSizeColumn(String sortBy) {
        AbstractDiskSizeColumn<Disk> column = new AbstractDiskSizeColumn<Disk>(SizeConverter.SizeUnit.GB) {
            @Override
            protected Long getRawValue(Disk object) {
                return object.getDiskStorageType() == DiskStorageType.IMAGE ?
                        Math.round(((DiskImage) object).getActualDiskWithSnapshotsSize())
                        : (long) (((LunDisk) object).getLun().getDeviceSize());
            }
        };

        return makeSortable(column, sortBy);
    }

    public static final AbstractTextColumn<Disk> getAllocationColumn(String sortBy) {
        AbstractTextColumn<Disk> column = new AbstractEnumColumn<Disk, VolumeType>() {
            @Override
            protected VolumeType getRawValue(Disk object) {
                return object.getDiskStorageType() == DiskStorageType.IMAGE ?
                        ((DiskImage) object).getVolumeType() : null;
            }
        };

        return makeSortable(column, sortBy);
    }

    public static final AbstractTextColumn<Disk> getInterfaceColumn(String sortBy) {
        AbstractTextColumn<Disk> column = new AbstractEnumColumn<Disk, DiskInterface>() {
            @Override
            protected DiskInterface getRawValue(Disk object) {
                return object.getDiskInterface();
            }
        };

        return makeSortable(column, sortBy);
    }

    public static final AbstractTextColumn<Disk> getDateCreatedColumn(String sortBy) {
        AbstractTextColumn<Disk> column = new AbstractFullDateTimeColumn<Disk>() {
            @Override
            protected Date getRawValue(Disk object) {
                return object.getDiskStorageType() == DiskStorageType.IMAGE ?
                        ((DiskImage) object).getCreationDate() : null;
            }
        };

        return makeSortable(column, sortBy);
    }

    public static final AbstractTextColumn<Disk> getStatusColumn(String sortBy) {
        AbstractTextColumn<Disk> column = new AbstractEnumColumn<Disk, ImageStatus>() {
            @Override
            protected ImageStatus getRawValue(Disk object) {
                return object.getDiskStorageType() == DiskStorageType.IMAGE ?
                        ((DiskImage) object).getImageStatus() : null;
            }
        };

        return makeSortable(column, sortBy);
    }

    public static final AbstractTextColumn<Disk> getDescriptionColumn(String sortBy) {
        AbstractTextColumn<Disk> column = new AbstractTextColumn<Disk>() {
            @Override
            public String getValue(Disk object) {
                return object.getDiskDescription();
            }
        };

        return makeSortable(column, sortBy);
    }

    public static final AbstractTextColumn<Disk> getLunIdColumn(String sortBy) {
        AbstractTextColumn<Disk> column = new AbstractTextColumn<Disk>() {
            @Override
            public String getValue(Disk object) {
                return object.getDiskStorageType() == DiskStorageType.LUN ?
                        ((LunDisk) object).getLun().getLUN_id() : null;
            }
        };

        return makeSortable(column, sortBy);
    }

    public static final AbstractTextColumn<Disk> getLunVendorIdColumn(String sortBy) {
        AbstractTextColumn<Disk> column = new AbstractTextColumn<Disk>() {
            @Override
            public String getValue(Disk object) {
                return object.getDiskStorageType() == DiskStorageType.LUN ?
                        ((LunDisk) object).getLun().getVendorId() : null;
            }
        };

        return makeSortable(column, sortBy);
    }

    public static final AbstractTextColumn<Disk> getLunProductIdColumn(String sortBy) {
        AbstractTextColumn<Disk> column = new AbstractTextColumn<Disk>() {
            @Override
            public String getValue(Disk object) {
                return object.getDiskStorageType() == DiskStorageType.LUN ?
                        ((LunDisk) object).getLun().getProductId() : null;
            }
        };

        return makeSortable(column, sortBy);
    }

    public static final AbstractTextColumn<Disk> getLunSerialColumn(String sortBy) {
        AbstractTextColumn<Disk> column = new AbstractTextColumn<Disk>() {
            @Override
            public String getValue(Disk object) {
                return object.getDiskStorageType() == DiskStorageType.LUN ?
                        ((LunDisk) object).getLun().getSerial() : null;
            }
        };

        return makeSortable(column, sortBy);
    }

    public static final AbstractCheckboxColumn<EntityModel> readOnlyCheckboxColumn = new AbstractCheckboxColumn<EntityModel>(
        new FieldUpdater<EntityModel, Boolean>() {
            @Override
            public void update(int idx, EntityModel object, Boolean value) {
                DiskModel diskModel = (DiskModel) object.getEntity();
                diskModel.getDisk().setReadOnly(value);
            }
        }) {
            @Override
            protected boolean canEdit(EntityModel object) {
                DiskModel diskModel = (DiskModel) object.getEntity();
                Disk disk = diskModel.getDisk();
                boolean isScsiPassthrough = disk.isScsiPassthrough();
                boolean ideLimitation = (disk.getDiskInterface() == DiskInterface.IDE);
                return !isScsiPassthrough && !ideLimitation;
            }

            @Override
            public Boolean getValue(EntityModel object) {
                DiskModel diskModel = (DiskModel) object.getEntity();
                return diskModel.getDisk().getReadOnly();
            }
    };

    public static final AbstractDiskSizeColumn<Disk> getSnapshotSizeColumn(String sortBy) {
        AbstractDiskSizeColumn<Disk> column = new AbstractDiskSizeColumn<Disk>() {
            @Override
            protected Long getRawValue(Disk object) {
                return ((DiskImage) object).getActualSizeInBytes();
            }
        };

        return makeSortable(column, sortBy);
    }

    public static final AbstractTextColumn<Disk> getSnapshotDescriptionColumn(String sortBy) {
        AbstractTextColumn<Disk> column = new AbstractTextColumn<Disk>() {
            @Override
            public String getValue(Disk object) {
                return ((DiskImage) object).getVmSnapshotDescription();
            }
        };

        return makeSortable(column, sortBy);
    }

    public static final AbstractTextColumn<Disk> getDiskSnapshotIDColumn(String sortBy) {
        AbstractTextColumn<Disk> column = new AbstractTextColumn<Disk>() {
            @Override
            public String getValue(Disk disk) {
                return disk.getDiskStorageType() == DiskStorageType.IMAGE ?
                        ((DiskImage) disk).getImageId().toString() : null;
            }
        };

        return makeSortable(column, sortBy);
    }

    public static <C extends AbstractTextColumn<T>, T> C makeSortable(C column, String sortBy) {
        if (sortBy == null ) {
            // Client sorting
            column.makeSortable();
        }
        else if (!sortBy.equals(constants.empty())) {
            // Server sorting
            column.makeSortable(sortBy);
        }

        return column;
    }
}
