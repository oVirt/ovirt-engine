package org.ovirt.engine.ui.common.widget.uicommon.template;

import java.util.Date;

import org.ovirt.engine.core.common.businessentities.storage.DiskImage;
import org.ovirt.engine.core.common.businessentities.storage.DiskInterface;
import org.ovirt.engine.core.common.businessentities.storage.ImageStatus;
import org.ovirt.engine.core.common.businessentities.storage.VolumeType;
import org.ovirt.engine.core.common.utils.SizeConverter;
import org.ovirt.engine.ui.common.CommonApplicationConstants;
import org.ovirt.engine.ui.common.gin.AssetProvider;
import org.ovirt.engine.ui.common.system.ClientStorage;
import org.ovirt.engine.ui.common.uicommon.model.SearchableTableModelProvider;
import org.ovirt.engine.ui.common.widget.table.column.AbstractDiskSizeColumn;
import org.ovirt.engine.ui.common.widget.table.column.AbstractEnumColumn;
import org.ovirt.engine.ui.common.widget.table.column.AbstractFullDateTimeColumn;
import org.ovirt.engine.ui.common.widget.table.column.AbstractTextColumn;
import org.ovirt.engine.ui.common.widget.uicommon.AbstractModelBoundTableWidget;
import org.ovirt.engine.ui.uicommonweb.models.templates.TemplateDiskListModel;
import com.google.gwt.event.shared.EventBus;

public class TemplateDiskListModelTable<T extends TemplateDiskListModel> extends AbstractModelBoundTableWidget<DiskImage, T> {

    private static final CommonApplicationConstants constants = AssetProvider.getConstants();

    public TemplateDiskListModelTable(
            SearchableTableModelProvider<DiskImage, T> modelProvider,
            EventBus eventBus, ClientStorage clientStorage) {
        super(modelProvider, eventBus, clientStorage, false);
    }

    @Override
    public void initTable() {
        getTable().enableColumnResizing();

        AbstractTextColumn<DiskImage> aliasColumn = new AbstractTextColumn<DiskImage>() {
            @Override
            public String getValue(DiskImage object) {
                return object.getDiskAlias();
            }
        };
        getTable().addColumn(aliasColumn, constants.aliasDisk(), "150px"); //$NON-NLS-1$

        AbstractTextColumn<DiskImage> provisionedSizeColumn = new AbstractTextColumn<DiskImage>() {
            @Override
            public String getValue(DiskImage object) {
                return String.valueOf(object.getSizeInGigabytes()) + " GB"; //$NON-NLS-1$
            }
        };
        getTable().addColumn(provisionedSizeColumn, constants.provisionedSizeDisk(), "150px"); //$NON-NLS-1$

        AbstractDiskSizeColumn<DiskImage> actualSizeColumn = new AbstractDiskSizeColumn<DiskImage>(SizeConverter.SizeUnit.GiB) {
            @Override
            protected Long getRawValue(DiskImage object) {
                       return Math.round(object.getActualDiskWithSnapshotsSize());
            }
        };

        getTable().addColumn(actualSizeColumn, constants.sizeDisk(), "150px"); //$NON-NLS-1$

        AbstractTextColumn<DiskImage> statusColumn = new AbstractEnumColumn<DiskImage, ImageStatus>() {
            @Override
            protected ImageStatus getRawValue(DiskImage object) {
                return object.getImageStatus();
            }
        };

        getTable().addColumn(statusColumn, constants.statusDisk(), "150px"); //$NON-NLS-1$

        AbstractTextColumn<DiskImage> allocationColumn = new AbstractEnumColumn<DiskImage, VolumeType>() {
            @Override
            protected VolumeType getRawValue(DiskImage object) {
                return VolumeType.forValue(object.getVolumeType().getValue());
            }
        };
        getTable().addColumn(allocationColumn, constants.allocationDisk(), "150px"); //$NON-NLS-1$

        AbstractTextColumn<DiskImage> interfaceColumn = new AbstractEnumColumn<DiskImage, DiskInterface>() {
            @Override
            protected DiskInterface getRawValue(DiskImage object) {
                if (object.getDiskVmElements().size() == 1) {
                    return object.getDiskVmElements().iterator().next().getDiskInterface();
                }
                return null;
            }
        };
        getTable().addColumn(interfaceColumn, constants.interfaceDisk(), "150px"); //$NON-NLS-1$

        AbstractTextColumn<DiskImage> dateCreatedColumn = new AbstractFullDateTimeColumn<DiskImage>() {
            @Override
            protected Date getRawValue(DiskImage object) {
                return object.getCreationDate();
            }
        };

        getTable().addColumn(dateCreatedColumn, constants.creationDateDisk(), "150px"); //$NON-NLS-1$

        AbstractTextColumn<DiskImage> descriptionColumn = new AbstractTextColumn<DiskImage>() {
            @Override
            public String getValue(DiskImage object) {
                return object.getDescription();
            }
        };

        getTable().addColumn(descriptionColumn, constants.descriptionDisk(), "150px"); //$NON-NLS-1$
    }

}
