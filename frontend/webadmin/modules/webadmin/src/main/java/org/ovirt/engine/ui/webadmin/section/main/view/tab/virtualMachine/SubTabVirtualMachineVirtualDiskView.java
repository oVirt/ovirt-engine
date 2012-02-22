package org.ovirt.engine.ui.webadmin.section.main.view.tab.virtualMachine;

import java.util.Date;

import org.ovirt.engine.core.common.businessentities.DiskImage;
import org.ovirt.engine.core.common.businessentities.VM;
import org.ovirt.engine.core.common.businessentities.VolumeType;
import org.ovirt.engine.core.compat.Event;
import org.ovirt.engine.core.compat.EventArgs;
import org.ovirt.engine.core.compat.IEventListener;
import org.ovirt.engine.core.compat.PropertyChangedEventArgs;
import org.ovirt.engine.ui.common.idhandler.ElementIdHandler;
import org.ovirt.engine.ui.common.uicommon.model.SearchableDetailModelProvider;
import org.ovirt.engine.ui.common.widget.table.column.EnumColumn;
import org.ovirt.engine.ui.common.widget.table.column.FullDateTimeColumn;
import org.ovirt.engine.ui.common.widget.table.column.TextColumnWithTooltip;
import org.ovirt.engine.ui.uicommonweb.UICommand;
import org.ovirt.engine.ui.uicommonweb.models.vms.VmDiskListModel;
import org.ovirt.engine.ui.uicommonweb.models.vms.VmListModel;
import org.ovirt.engine.ui.webadmin.ApplicationConstants;
import org.ovirt.engine.ui.webadmin.ApplicationResources;
import org.ovirt.engine.ui.webadmin.section.main.presenter.tab.virtualMachine.SubTabVirtualMachineVirtualDiskPresenter;
import org.ovirt.engine.ui.webadmin.section.main.view.AbstractSubTabTableView;
import org.ovirt.engine.ui.webadmin.widget.action.WebAdminButtonDefinition;
import org.ovirt.engine.ui.webadmin.widget.action.WebAdminImageButtonDefinition;
import org.ovirt.engine.ui.webadmin.widget.table.column.DiskSizeColumn;
import org.ovirt.engine.ui.webadmin.widget.table.column.ImageResourceColumn;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.logical.shared.InitializeEvent;
import com.google.gwt.resources.client.ImageResource;
import com.google.inject.Inject;

public class SubTabVirtualMachineVirtualDiskView extends AbstractSubTabTableView<VM, DiskImage, VmListModel, VmDiskListModel> implements SubTabVirtualMachineVirtualDiskPresenter.ViewDef {

    interface ViewIdHandler extends ElementIdHandler<SubTabVirtualMachineVirtualDiskView> {
        ViewIdHandler idHandler = GWT.create(ViewIdHandler.class);
    }

    private WebAdminImageButtonDefinition plugButtonDefinition;
    private WebAdminImageButtonDefinition unPlugButtonDefinition;

    @Inject
    public SubTabVirtualMachineVirtualDiskView(SearchableDetailModelProvider<DiskImage, VmListModel, VmDiskListModel> modelProvider,
            ApplicationResources resources,
            ApplicationConstants constants) {
        super(modelProvider);
        ViewIdHandler.idHandler.generateAndSetIds(this);
        initTable(resources, constants);
        initWidget(getTable());

        getDetailModel().getPropertyChangedEvent().addListener(new IEventListener() {
            @Override
            public void eventRaised(Event ev, Object sender, EventArgs args) {
                PropertyChangedEventArgs changedArgs = (PropertyChangedEventArgs) args;
                if ("IsDiskHotPlugAvailable".equals(changedArgs.PropertyName)) {
                    InitializeEvent.fire(plugButtonDefinition);
                    InitializeEvent.fire(unPlugButtonDefinition);
                }
            }
        });

        getDetailModel().getSelectedItemChangedEvent().addListener(new IEventListener() {
            @Override
            public void eventRaised(Event ev, Object sender, EventArgs args) {
                InitializeEvent.fire(plugButtonDefinition);
                InitializeEvent.fire(unPlugButtonDefinition);
            }
        });
    }

    private void initTable(final ApplicationResources resources, final ApplicationConstants constants) {

        getTable().addColumn(new ImageResourceColumn<DiskImage>() {
            @Override
            public ImageResource getValue(DiskImage object) {
                return (object.getPlugged() != null && object.getPlugged().booleanValue()) ?
                        resources.upImage() : resources.downImage();
            }
        }, "", "30px");

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

        getTable().addActionButton(new WebAdminButtonDefinition<DiskImage>("New") {
            @Override
            protected UICommand resolveCommand() {
                return getDetailModel().getNewCommand();
            }
        });

        getTable().addActionButton(new WebAdminButtonDefinition<DiskImage>("Edit") {
            @Override
            protected UICommand resolveCommand() {
                return getDetailModel().getEditCommand();
            }
        });

        getTable().addActionButton(new WebAdminButtonDefinition<DiskImage>("Remove") {
            @Override
            protected UICommand resolveCommand() {
                return getDetailModel().getRemoveCommand();
            }
        });

        plugButtonDefinition = new WebAdminImageButtonDefinition<DiskImage>("Activate",
                resources.upImage(), resources.upDisabledImage(), true) {
            @Override
            protected UICommand resolveCommand() {
                return getDetailModel().getPlugCommand();
            }

            @Override
            public String getCustomToolTip() {
                DiskImage disk = (DiskImage) getDetailModel().getSelectedItem();
                if (!getDetailModel().isVmDown() && getDetailModel().isHotPlugAvailable()
                        && !getDetailModel().getIsDiskHotPlugSupported()) {
                    return constants.diskHotPlugNotSupported();
                }
                else {
                    return this.getTitle();
                }
            }
        };

        unPlugButtonDefinition = new WebAdminImageButtonDefinition<DiskImage>("Deactivate",
                resources.downImage(), resources.downDisabledImage(), true) {
            @Override
            protected UICommand resolveCommand() {
                return getDetailModel().getUnPlugCommand();
            }

            @Override
            public String getCustomToolTip() {
                DiskImage disk = (DiskImage) getDetailModel().getSelectedItem();
                if (!getDetailModel().isVmDown() && getDetailModel().isHotPlugAvailable()
                        && !getDetailModel().getIsDiskHotPlugSupported()) {
                    return constants.diskHotPlugNotSupported();
                }
                else {
                    return this.getTitle();
                }
            }
        };

        getTable().addActionButton(plugButtonDefinition);

        getTable().addActionButton(unPlugButtonDefinition);
    }
}
