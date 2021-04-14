package org.ovirt.engine.ui.common.widget.uicommon.disks;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.function.Predicate;

import org.ovirt.engine.core.common.businessentities.storage.Disk;
import org.ovirt.engine.core.common.businessentities.storage.DiskContentType;
import org.ovirt.engine.core.common.businessentities.storage.DiskImage;
import org.ovirt.engine.core.common.businessentities.storage.DiskInterface;
import org.ovirt.engine.core.common.businessentities.storage.DiskStorageType;
import org.ovirt.engine.core.common.businessentities.storage.ImageStatus;
import org.ovirt.engine.core.common.businessentities.storage.LunDisk;
import org.ovirt.engine.core.common.businessentities.storage.StorageType;
import org.ovirt.engine.core.common.businessentities.storage.VolumeType;
import org.ovirt.engine.core.common.utils.SizeConverter;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.compat.StringFormat;
import org.ovirt.engine.ui.common.CommonApplicationConstants;
import org.ovirt.engine.ui.common.CommonApplicationResources;
import org.ovirt.engine.ui.common.gin.AssetProvider;
import org.ovirt.engine.ui.common.widget.table.cell.Cell;
import org.ovirt.engine.ui.common.widget.table.cell.StatusCompositeCell;
import org.ovirt.engine.ui.common.widget.table.column.AbstractColumn;
import org.ovirt.engine.ui.common.widget.table.column.AbstractDiskSizeColumn;
import org.ovirt.engine.ui.common.widget.table.column.AbstractEnumColumn;
import org.ovirt.engine.ui.common.widget.table.column.AbstractFullDateTimeColumn;
import org.ovirt.engine.ui.common.widget.table.column.AbstractImageResourceColumn;
import org.ovirt.engine.ui.common.widget.table.column.AbstractLinkColumn;
import org.ovirt.engine.ui.common.widget.table.column.AbstractTextColumn;
import org.ovirt.engine.ui.common.widget.table.column.DiskContainersColumn;
import org.ovirt.engine.ui.common.widget.table.column.DiskProgressColumn;
import org.ovirt.engine.ui.common.widget.table.column.DiskStatusColumn;
import org.ovirt.engine.ui.common.widget.table.column.DiskTransferProgressColumn;
import org.ovirt.engine.ui.common.widget.table.column.StorageDomainsColumn;
import org.ovirt.engine.ui.uicompat.EnumTranslator;

import com.google.gwt.cell.client.FieldUpdater;
import com.google.gwt.cell.client.HasCell;
import com.google.gwt.resources.client.ImageResource;
import com.google.gwt.safehtml.shared.SafeHtml;
import com.google.gwt.safehtml.shared.SafeHtmlUtils;

public class DisksViewColumns {

    private static final CommonApplicationResources resources = AssetProvider.getResources();
    private static final CommonApplicationConstants constants = AssetProvider.getConstants();

    private static final Predicate<Disk> diskImagePredicate =
            disk -> disk.getDiskStorageType() == DiskStorageType.IMAGE ||
                    disk.getDiskStorageType() == DiskStorageType.MANAGED_BLOCK_STORAGE;

    public static AbstractTextColumn<Disk> getAliasColumn(String sortBy) {
        return getAliasColumn(null, sortBy);
    }

    public static AbstractTextColumn<Disk> getAliasColumn(FieldUpdater<Disk, String> updater, String sortBy) {
        AbstractTextColumn<Disk> column;

        if (updater != null) {
            column = new AbstractLinkColumn<Disk>(updater) {
                @Override
                public String getValue(Disk object) {
                    return object.getName();
                }
            };
        } else {
            column = new AbstractTextColumn<Disk>() {
                @Override
                public String getValue(Disk object) {
                    return object.getName();
                }
            };
        }

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
                    List<String> quotaNamesArr = diskImage.getQuotaNames();
                    if (quotaNamesArr != null) {
                        value = String.join(", ", quotaNamesArr);//$NON-NLS-1$
                    }
                }
                return value;
            }
        };

        return makeSortable(column, sortBy);
    }

    public static final AbstractImageResourceColumn<Disk> bootableDiskColumn = new AbstractImageResourceColumn<Disk>() {
        {
            setContextMenuTitle(constants.bootableDisk());
        }

        @Override
        public ImageResource getValue(Disk object) {
            if (object.getDiskVmElements().size() == 1) {
                if (object.getDiskVmElements().iterator().next().isBoot()) {
                    return getDefaultImage();
                }
            }
            return null;
        }

        @Override
        public ImageResource getDefaultImage() {
            return resources.bootableDiskIcon();
        }

        @Override
        public SafeHtml getTooltip(Disk object) {
            if (object.getDiskVmElements().size() == 1) {
                if (object.getDiskVmElements().iterator().next().isBoot()) {
                    return SafeHtmlUtils.fromSafeConstant(constants.bootableDisk());
                }
            }
            return null;
        }
    };

    public static AbstractImageResourceColumn<Disk> getShareableDiskColumn() {
        AbstractImageResourceColumn<Disk> shareableDiskColumn = new AbstractImageResourceColumn<Disk>() {
            {
                setContextMenuTitle(constants.shareable());
            }

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
        shareableDiskColumn.makeSortable(Comparator.comparing(Disk::isShareable));
        return shareableDiskColumn;
    }

    public static final AbstractImageResourceColumn<Disk> readOnlyDiskColumn = new AbstractImageResourceColumn<Disk>() {
        {
            setContextMenuTitle(constants.readOnly());
        }

        @Override
        public ImageResource getValue(Disk object) {
            if (object.getDiskVmElements().size() == 1) {
                return object.getDiskVmElements().iterator().next().isReadOnly() ? getDefaultImage() : null;
            }
            return null;
        }

        @Override
        public ImageResource getDefaultImage() {
            return resources.readOnlyDiskIcon();
        }

        @Override
        public SafeHtml getTooltip(Disk object) {
            if (object.getDiskVmElements().size() == 1) {
                return object.getDiskVmElements().iterator().next().isReadOnly() ?
                        SafeHtmlUtils.fromSafeConstant(constants.readOnly()) : null;
            }
            return null;
        }
    };

    public static final AbstractImageResourceColumn<Disk> diskContainersIconColumn = new AbstractImageResourceColumn<Disk>() {
        {
            setContextMenuTitle(constants.containersIconDisk());
        }

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
            } else {
                String status = EnumTranslator.getInstance().translate(object.getVmEntityType());
                return SafeHtmlUtils.fromString(status);
            }
        }
    };

    public static final DiskStatusColumn diskStatusColumn = new DiskStatusColumn() {
        {
            setContextMenuTitle(constants.statusDisk());
        }
    };

    public static final DiskContainersColumn getdiskContainersColumn(String sortBy){
        DiskContainersColumn diskContainersColumn = new DiskContainersColumn();
        makeSortable(diskContainersColumn, sortBy);
        return diskContainersColumn;
    }

    public static final StorageDomainsColumn getStorageDomainsColumn(String sortBy) {
        StorageDomainsColumn storageDomainsColumn = new StorageDomainsColumn();
        makeSortable(storageDomainsColumn, sortBy);
        return storageDomainsColumn;
    }

    public static final AbstractTextColumn<Disk> storageTypeColumn = new AbstractEnumColumn<Disk, StorageType>() {
        @Override
        protected StorageType getRawValue(Disk object) {
            if (!diskImagePredicate.test(object)) {
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
                switch (object.getDiskStorageType()) {
                    case LUN:
                        return (long) (((LunDisk) object).getLun().getDeviceSize() * Math.pow(1024, 3));
                    default:
                        return object.getSize();
                }
            }
        };

        return makeSortable(column, sortBy);
    }

    public static final AbstractDiskSizeColumn<Disk> getActualSizeColumn(String sortBy) {
        AbstractDiskSizeColumn<Disk> column = new AbstractDiskSizeColumn<Disk>(SizeConverter.SizeUnit.GiB) {
            @Override
            protected Long getRawValue(Disk object) {
                return diskImagePredicate.test(object) ?
                        Math.round(((DiskImage) object).getActualDiskWithSnapshotsSize())
                        : (long) ((LunDisk) object).getLun().getDeviceSize();
            }
        };

        return makeSortable(column, sortBy);
    }

    public static final AbstractTextColumn<Disk> getAllocationColumn(String sortBy) {
        AbstractTextColumn<Disk> column = new AbstractEnumColumn<Disk, VolumeType>() {
            @Override
            protected VolumeType getRawValue(Disk object) {
                return diskImagePredicate.test(object) ? ((DiskImage) object).getVolumeType() : null;
            }

            @Override
            public SafeHtml getTooltip(Disk object) {
                if (!diskImagePredicate.test(object)) {
                    return null;
                }

                VolumeType originalVolumeType = null;
                for (DiskImage snapshot : ((DiskImage) object).getSnapshots()) {
                    if (snapshot.getParentId() == null || snapshot.getParentId().equals(Guid.Empty)) {
                        originalVolumeType = snapshot.getVolumeType();
                        break;
                    }
                }

                if (originalVolumeType == null) {
                    return null;
                }

                return SafeHtmlUtils.fromString(
                        StringFormat.format("%s: %s",  //$NON-NLS-1$
                                AssetProvider.getConstants().originalAllocationDisk(),
                                EnumTranslator.getInstance().translate(originalVolumeType)));
            }
        };

        return makeSortable(column, sortBy);
    }

    public static final AbstractTextColumn<Disk> getInterfaceColumn(String sortBy) {
        AbstractTextColumn<Disk> column = new AbstractEnumColumn<Disk, DiskInterface>() {
            @Override
            protected DiskInterface getRawValue(Disk object) {
                if (object.getDiskVmElements().size() == 1) {
                    return object.getDiskVmElements().iterator().next().getDiskInterface();
                }
                return null;
            }
        };

        return makeSortable(column, sortBy);
    }

    public static AbstractTextColumn<Disk> getLogicalNameColumn(String sortBy) {
        AbstractTextColumn<Disk> column = new AbstractTextColumn<Disk>() {
            @Override
            public String getValue(Disk object) {
                if (object.getDiskVmElements().size() == 1) {
                    return object.getDiskVmElements().iterator().next().getLogicalName();
                }
                return null;
            }
        };

        return makeSortable(column, sortBy);
    }

    public static final AbstractFullDateTimeColumn<Disk> getDateCreatedColumn(String sortBy) {
        AbstractFullDateTimeColumn<Disk> column = new AbstractFullDateTimeColumn<Disk>() {
            @Override
            protected Date getRawValue(Disk object) {
                return diskImagePredicate.test(object) ? ((DiskImage) object).getCreationDate() : null;
            }
        };

        return makeSortable(column, sortBy);
    }

    public static final AbstractFullDateTimeColumn<Disk> getSnapshotCreationDateColumn(String sortBy) {
        AbstractFullDateTimeColumn<Disk> column = new AbstractFullDateTimeColumn<Disk>() {
            @Override
            protected Date getRawValue(Disk object) {
                return diskImagePredicate.test(object) ? ((DiskImage) object).getSnapshotCreationDate() : null;
            }
        };
        return makeSortable(column, sortBy);
    }

    public static final AbstractFullDateTimeColumn<Disk> getDateModifiedColumn(String sortBy) {
        AbstractFullDateTimeColumn<Disk> column = new AbstractFullDateTimeColumn<Disk>() {
            @Override
            protected Date getRawValue(Disk object) {
                return diskImagePredicate.test(object) ? ((DiskImage) object).getLastModified() : null;
            }
        };

        return makeSortable(column, sortBy);
    }

    public static final AbstractColumn<Disk, Disk> getStatusColumn(String sortBy) {
        DiskTransferProgressColumn uploadImageProgressColumn = new DiskTransferProgressColumn();
        DiskProgressColumn diskProgressColumn = new DiskProgressColumn();

        List<HasCell<Disk, ?>> list = new ArrayList<>();
        list.add(getStatusOnlyColumn(null));
        list.add(uploadImageProgressColumn);
        list.add(diskProgressColumn);

        Cell<Disk> compositeCell = new StatusCompositeCell<>(list);

        AbstractColumn<Disk, Disk> column = new AbstractColumn<Disk, Disk>(compositeCell) {
            @Override
            public Disk getValue(Disk object) {
                return object;
            }
        };

        if (sortBy != null) {
            column.makeSortable(sortBy);
        }
        return column;
    }

    public static final AbstractTextColumn<Disk> getStatusOnlyColumn(String sortBy) {
        AbstractTextColumn<Disk> column = new AbstractEnumColumn<Disk, ImageStatus>() {
            @Override
            protected ImageStatus getRawValue(Disk object) {
                return diskImagePredicate.test(object) ? ((DiskImage) object).getImageStatus() : null;
            }

            @Override
            public String getValue(Disk object) {
                if (object.getImageTransferPhase() != null) {
                    // will be rendered by progress column
                    return null;
                }
                return super.getValue(object);
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

    public static final AbstractTextColumn<Disk> getContentColumn(String sortBy) {
        AbstractTextColumn<Disk> column = new AbstractEnumColumn<Disk, DiskContentType>() {
            @Override
            protected DiskContentType getRawValue(Disk object) {
                return object.getContentType();
            }
        };

        return makeSortable(column, sortBy);
    }

    public static final AbstractTextColumn<Disk> getLunIdColumn(String sortBy) {
        AbstractTextColumn<Disk> column = new AbstractTextColumn<Disk>() {
            @Override
            public String getValue(Disk object) {
                return object.getDiskStorageType() == DiskStorageType.LUN ?
                        ((LunDisk) object).getLun().getLUNId() : null;
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
                return diskImagePredicate.test(disk) ? ((DiskImage) disk).getImageId().toString() : null;
            }
        };

        return makeSortable(column, sortBy);
    }

    public static final AbstractTextColumn<Disk> getDiskStorageTypeColumn(String sortBy) {
        AbstractTextColumn<Disk> column = new AbstractEnumColumn<Disk, DiskStorageType>() {
            @Override
            protected DiskStorageType getRawValue(Disk object) {
                return object.getDiskStorageType();
            }
        };

        return makeSortable(column, sortBy);
    }

    public static <C extends AbstractTextColumn<T>, T> C makeSortable(C column, String sortBy) {
        if (sortBy == null ) {
            // Client sorting
            column.makeSortable();
        } else if (!sortBy.equals(constants.empty())) {
            // Server sorting
            column.makeSortable(sortBy);
        }

        return column;
    }
}
