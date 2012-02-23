package org.ovirt.engine.ui.common.widget.uicommon.vm;

import java.util.Date;

import org.ovirt.engine.core.common.businessentities.DiskImage;
import org.ovirt.engine.core.common.businessentities.VolumeType;
import org.ovirt.engine.core.compat.Event;
import org.ovirt.engine.core.compat.EventArgs;
import org.ovirt.engine.core.compat.IEventListener;
import org.ovirt.engine.core.compat.PropertyChangedEventArgs;
import org.ovirt.engine.ui.common.CommonApplicationConstants;
import org.ovirt.engine.ui.common.CommonApplicationResources;
import org.ovirt.engine.ui.common.system.ClientStorage;
import org.ovirt.engine.ui.common.uicommon.model.SearchableTableModelProvider;
import org.ovirt.engine.ui.common.widget.action.ImageUiCommandButtonDefinition;
import org.ovirt.engine.ui.common.widget.action.UiCommandButtonDefinition;
import org.ovirt.engine.ui.common.widget.table.column.BaseDiskImageStatusColumn;
import org.ovirt.engine.ui.common.widget.table.column.DiskSizeColumn;
import org.ovirt.engine.ui.common.widget.table.column.EnumColumn;
import org.ovirt.engine.ui.common.widget.table.column.FullDateTimeColumn;
import org.ovirt.engine.ui.common.widget.table.column.TextColumnWithTooltip;
import org.ovirt.engine.ui.common.widget.uicommon.AbstractModelBoundTableWidget;
import org.ovirt.engine.ui.uicommonweb.UICommand;
import org.ovirt.engine.ui.uicommonweb.models.vms.VmDiskListModel;

import com.google.gwt.event.logical.shared.InitializeEvent;
import com.google.gwt.event.shared.EventBus;

public class VmDiskListModelTable extends AbstractModelBoundTableWidget<DiskImage, VmDiskListModel> {

    private final BaseDiskImageStatusColumn statusColumn;
    private final CommonApplicationResources resources;
    private final CommonApplicationConstants constants;

    private ImageUiCommandButtonDefinition<DiskImage> plugButtonDefinition;
    private ImageUiCommandButtonDefinition<DiskImage> unPlugButtonDefinition;

    public VmDiskListModelTable(
            SearchableTableModelProvider<DiskImage, VmDiskListModel> modelProvider,
            EventBus eventBus, ClientStorage clientStorage,
            BaseDiskImageStatusColumn statusColumn,
            CommonApplicationResources resources,
            CommonApplicationConstants constants) {
        super(modelProvider, eventBus, clientStorage, false);
        this.statusColumn = statusColumn;
        this.resources = resources;
        this.constants = constants;

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
        getTable().addColumn(statusColumn, "", "30px");

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

        TextColumnWithTooltip<DiskImage> typeColumn = new TextColumnWithTooltip<DiskImage>() {
            @Override
            public String getValue(DiskImage object) {
                return object.getdisk_type().toString();
            }
        };
        getTable().addColumn(typeColumn, "Type");

        TextColumnWithTooltip<DiskImage> formatColumn = new TextColumnWithTooltip<DiskImage>() {
            @Override
            public String getValue(DiskImage object) {
                return object.getvolume_format().toString();
            }
        };
        getTable().addColumn(formatColumn, "Format");

        TextColumnWithTooltip<DiskImage> allocationColumn = new EnumColumn<DiskImage, VolumeType>() {
            @Override
            protected VolumeType getRawValue(DiskImage object) {
                return VolumeType.forValue(object.getvolume_type().getValue());
            }
        };
        getTable().addColumn(allocationColumn, "Allocation");

        TextColumnWithTooltip<DiskImage> interfaceColumn = new TextColumnWithTooltip<DiskImage>() {
            @Override
            public String getValue(DiskImage object) {
                return object.getdisk_interface().toString();
            }
        };
        getTable().addColumn(interfaceColumn, "Interface");

        TextColumnWithTooltip<DiskImage> dateCreatedColumn = new FullDateTimeColumn<DiskImage>() {
            @Override
            protected Date getRawValue(DiskImage object) {
                return object.getcreation_date();
            }
        };
        getTable().addColumn(dateCreatedColumn, "Date Created");

        getTable().addActionButton(new UiCommandButtonDefinition<DiskImage>(getEventBus(), "New") {
            @Override
            protected UICommand resolveCommand() {
                return getModel().getNewCommand();
            }
        });

        getTable().addActionButton(new UiCommandButtonDefinition<DiskImage>(getEventBus(), "Edit") {
            @Override
            protected UICommand resolveCommand() {
                return getModel().getEditCommand();
            }
        });

        getTable().addActionButton(new UiCommandButtonDefinition<DiskImage>(getEventBus(), "Remove") {
            @Override
            protected UICommand resolveCommand() {
                return getModel().getRemoveCommand();
            }
        });

        plugButtonDefinition = new ImageUiCommandButtonDefinition<DiskImage>(getEventBus(), "Activate",
                resources.upImage(), resources.upDisabledImage(), true, false) {
            @Override
            protected UICommand resolveCommand() {
                return getModel().getPlugCommand();
            }

            @Override
            public String getCustomToolTip() {
                if (!getModel().isVmDown() && getModel().isHotPlugAvailable()
                        && !getModel().getIsDiskHotPlugSupported()) {
                    return constants.diskHotPlugNotSupported();
                }
                else {
                    return this.getTitle();
                }
            }
        };
        getTable().addActionButton(plugButtonDefinition);

        unPlugButtonDefinition = new ImageUiCommandButtonDefinition<DiskImage>(getEventBus(), "Deactivate",
                resources.downImage(), resources.downDisabledImage(), true, false) {
            @Override
            protected UICommand resolveCommand() {
                return getModel().getUnPlugCommand();
            }

            @Override
            public String getCustomToolTip() {
                if (!getModel().isVmDown() && getModel().isHotPlugAvailable()
                        && !getModel().getIsDiskHotPlugSupported()) {
                    return constants.diskHotPlugNotSupported();
                }
                else {
                    return this.getTitle();
                }
            }
        };
        getTable().addActionButton(unPlugButtonDefinition);
    }

}
