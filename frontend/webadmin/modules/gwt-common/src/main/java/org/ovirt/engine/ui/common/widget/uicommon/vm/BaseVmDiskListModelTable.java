package org.ovirt.engine.ui.common.widget.uicommon.vm;

import java.util.Date;

import org.ovirt.engine.core.common.businessentities.DiskImage;
import org.ovirt.engine.core.common.businessentities.ImageStatus;
import org.ovirt.engine.core.common.businessentities.VolumeType;
import org.ovirt.engine.core.compat.Event;
import org.ovirt.engine.core.compat.EventArgs;
import org.ovirt.engine.core.compat.IEventListener;
import org.ovirt.engine.core.compat.PropertyChangedEventArgs;
import org.ovirt.engine.ui.common.system.ClientStorage;
import org.ovirt.engine.ui.common.uicommon.model.SearchableTableModelProvider;
import org.ovirt.engine.ui.common.widget.action.ImageUiCommandButtonDefinition;
import org.ovirt.engine.ui.common.widget.table.column.DiskImageStatusColumn;
import org.ovirt.engine.ui.common.widget.table.column.DiskSizeColumn;
import org.ovirt.engine.ui.common.widget.table.column.EnumColumn;
import org.ovirt.engine.ui.common.widget.table.column.FullDateTimeColumn;
import org.ovirt.engine.ui.common.widget.table.column.TextColumnWithTooltip;
import org.ovirt.engine.ui.common.widget.uicommon.AbstractModelBoundTableWidget;
import org.ovirt.engine.ui.uicommonweb.models.SearchableListModel;

import com.google.gwt.event.logical.shared.InitializeEvent;
import com.google.gwt.event.shared.EventBus;

public class BaseVmDiskListModelTable<T extends SearchableListModel> extends AbstractModelBoundTableWidget<DiskImage, T> {

    private ImageUiCommandButtonDefinition<DiskImage> plugButtonDefinition;
    private ImageUiCommandButtonDefinition<DiskImage> unPlugButtonDefinition;

    public BaseVmDiskListModelTable(
            SearchableTableModelProvider<DiskImage, T> modelProvider,
            EventBus eventBus,
            ClientStorage clientStorage) {
        super(modelProvider, eventBus, clientStorage, false);

        getModel().getPropertyChangedEvent().addListener(new IEventListener() {
            @Override
            public void eventRaised(Event ev, Object sender, EventArgs args) {
                PropertyChangedEventArgs changedArgs = (PropertyChangedEventArgs) args;
                if ("IsDiskHotPlugAvailable".equals(changedArgs.PropertyName)) {
                    InitializeEvent.fire(plugButtonDefinition);
                    InitializeEvent.fire(unPlugButtonDefinition);
                }
            }
        });

        getModel().getSelectedItemChangedEvent().addListener(new IEventListener() {
            @Override
            public void eventRaised(Event ev, Object sender, EventArgs args) {
                InitializeEvent.fire(plugButtonDefinition);
                InitializeEvent.fire(unPlugButtonDefinition);
            }
        });
    }

    @Override
    public void initTable() {
        getTable().addColumn(new DiskImageStatusColumn(), "", "30px");

        TextColumnWithTooltip<DiskImage> nameColumn = new TextColumnWithTooltip<DiskImage>() {
            @Override
            public String getValue(DiskImage object) {
                return "Disk " + object.getinternal_drive_mapping();
            }
        };
        getTable().addColumn(nameColumn, "Name");

        DiskSizeColumn<DiskImage> sizeColumn = new DiskSizeColumn<DiskImage>() {
            @Override
            protected Long getRawValue(DiskImage object) {
                return object.getsize();
            }
        };
        getTable().addColumn(sizeColumn, "Size");

        DiskSizeColumn<DiskImage> actualSizeColumn = new DiskSizeColumn<DiskImage>() {
            @Override
            protected Long getRawValue(DiskImage object) {
                return object.getactual_size();
            }
        };
        getTable().addColumn(actualSizeColumn, "Actual Size");

        TextColumnWithTooltip<DiskImage> storageDomainColumn = new TextColumnWithTooltip<DiskImage>() {
            @Override
            public String getValue(DiskImage object) {
                return object.getStoragesNames().get(0);
            }
        };
        getTable().addColumn(storageDomainColumn, "Storage Domain");

        TextColumnWithTooltip<DiskImage> typeColumn = new TextColumnWithTooltip<DiskImage>() {
            @Override
            public String getValue(DiskImage object) {
                return object.getdisk_type().toString();
            }
        };
        getTable().addColumn(typeColumn, "Type", "120px");

        TextColumnWithTooltip<DiskImage> allocationColumn = new EnumColumn<DiskImage, VolumeType>() {
            @Override
            protected VolumeType getRawValue(DiskImage object) {
                return VolumeType.forValue(object.getvolume_type().getValue());
            }
        };
        getTable().addColumn(allocationColumn, "Allocation", "120px");

        TextColumnWithTooltip<DiskImage> interfaceColumn = new TextColumnWithTooltip<DiskImage>() {
            @Override
            public String getValue(DiskImage object) {
                return object.getdisk_interface().toString();
            }
        };
        getTable().addColumn(interfaceColumn, "Interface", "120px");

        TextColumnWithTooltip<DiskImage> statusColumn = new EnumColumn<DiskImage, ImageStatus>() {
            @Override
            protected ImageStatus getRawValue(DiskImage object) {
                return object.getimageStatus();
            }
        };
        getTable().addColumn(statusColumn, "Status", "120px");

        TextColumnWithTooltip<DiskImage> dateCreatedColumn = new FullDateTimeColumn<DiskImage>() {
            @Override
            protected Date getRawValue(DiskImage object) {
                return object.getcreation_date();
            }
        };
        getTable().addColumn(dateCreatedColumn, "Creation Date", "140px");

    }

}
