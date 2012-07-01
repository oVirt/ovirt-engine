package org.ovirt.engine.ui.common.widget.uicommon.vm;

import java.util.Date;

import org.ovirt.engine.core.common.businessentities.Disk;
import org.ovirt.engine.core.common.businessentities.Disk.DiskStorageType;
import org.ovirt.engine.core.common.businessentities.DiskImage;
import org.ovirt.engine.core.common.businessentities.ImageStatus;
import org.ovirt.engine.core.common.businessentities.LunDisk;
import org.ovirt.engine.core.common.businessentities.VolumeType;
import org.ovirt.engine.ui.common.CommonApplicationConstants;
import org.ovirt.engine.ui.common.system.ClientStorage;
import org.ovirt.engine.ui.common.uicommon.model.SearchableTableModelProvider;
import org.ovirt.engine.ui.common.widget.table.column.DiskSizeColumn;
import org.ovirt.engine.ui.common.widget.table.column.DiskStatusColumn;
import org.ovirt.engine.ui.common.widget.table.column.EnumColumn;
import org.ovirt.engine.ui.common.widget.table.column.FullDateTimeColumn;
import org.ovirt.engine.ui.common.widget.table.column.TextColumnWithTooltip;
import org.ovirt.engine.ui.common.widget.uicommon.AbstractModelBoundTableWidget;
import org.ovirt.engine.ui.uicommonweb.models.SearchableListModel;

import com.google.gwt.event.shared.EventBus;

public class BaseVmDiskListModelTable<T extends SearchableListModel> extends AbstractModelBoundTableWidget<Disk, T> {

    public BaseVmDiskListModelTable(
            SearchableTableModelProvider<Disk, T> modelProvider,
            EventBus eventBus,
            ClientStorage clientStorage) {
        super(modelProvider, eventBus, clientStorage, false);
    }

    @Override
    public void initTable(final CommonApplicationConstants constants) {
        getTable().addColumn(new DiskStatusColumn(), constants.empty(), "70px"); //$NON-NLS-1$

        TextColumnWithTooltip<Disk> nameColumn = new TextColumnWithTooltip<Disk>() {
            @Override
            public String getValue(Disk object) {
                return object.getDiskAlias();
            }
        };
        getTable().addColumn(nameColumn, constants.aliasDisk());

        DiskSizeColumn<Disk> sizeColumn = new DiskSizeColumn<Disk>() {
            @Override
            protected Long getRawValue(Disk object) {
                return object.getDiskStorageType() == DiskStorageType.IMAGE ?
                        ((DiskImage) object).getsize() :
                        (long) (((LunDisk) object).getLun().getDeviceSize() * Math.pow(1024, 3));
            }
        };

        getTable().addColumn(sizeColumn, constants.provisionedSizeDisk());

        DiskSizeColumn<Disk> actualSizeColumn = new DiskSizeColumn<Disk>() {
            @Override
            protected Long getRawValue(Disk object) {
                return object.getDiskStorageType() == DiskStorageType.IMAGE ?
                        ((DiskImage) object).getactual_size()
                        : (long) (((LunDisk) object).getLun().getDeviceSize() * Math.pow(1024, 3));
            }
        };
        getTable().addColumn(actualSizeColumn, constants.sizeDisk());

        TextColumnWithTooltip<Disk> storageDomainColumn = new TextColumnWithTooltip<Disk>() {
            @Override
            public String getValue(Disk object) {
                if (object.getDiskStorageType() == DiskStorageType.IMAGE) {
                    DiskImage diskImage = (DiskImage)object;
                    if (diskImage.getStoragesNames() != null) {
                        return diskImage.getStoragesNames().get(0);
                    }
                }
                return constants.notAvailableLabel();
            }
        };
        getTable().addColumn(storageDomainColumn, constants.storageDomainDisk());

        TextColumnWithTooltip<Disk> allocationColumn = new EnumColumn<Disk, VolumeType>() {
            @Override
            protected VolumeType getRawValue(Disk object) {
                return object.getDiskStorageType() == DiskStorageType.IMAGE ?
                        ((DiskImage) object).getvolume_type() : null;
            }
        };
        getTable().addColumn(allocationColumn, constants.allocationDisk(), "120px"); //$NON-NLS-1$

        TextColumnWithTooltip<Disk> interfaceColumn = new TextColumnWithTooltip<Disk>() {
            @Override
            public String getValue(Disk object) {
                return object.getDiskInterface().toString();
            }
        };
        getTable().addColumn(interfaceColumn, constants.interfaceDisk(), "100px"); //$NON-NLS-1$

        TextColumnWithTooltip<Disk> statusColumn = new EnumColumn<Disk, ImageStatus>() {
            @Override
            protected ImageStatus getRawValue(Disk object) {
                return object.getDiskStorageType() == DiskStorageType.IMAGE ?
                        ((DiskImage) object).getimageStatus() : null;
            }
        };

        getTable().addColumn(statusColumn, constants.statusDisk(), "100px"); //$NON-NLS-1$

        TextColumnWithTooltip<Disk> dateCreatedColumn = new FullDateTimeColumn<Disk>() {
            @Override
            protected Date getRawValue(Disk object) {
                return object.getDiskStorageType() == DiskStorageType.IMAGE ?
                        ((DiskImage) object).getcreation_date() : null;
            }
        };
        getTable().addColumn(dateCreatedColumn, constants.creationDateDisk(), "140px"); //$NON-NLS-1$

        TextColumnWithTooltip<Disk> descriptionColumn = new TextColumnWithTooltip<Disk>() {
            @Override
            public String getValue(Disk object) {
                return object.getDiskDescription();
            }
        };
        getTable().addColumn(descriptionColumn, constants.descriptionDisk());
    }

}
