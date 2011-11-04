package org.ovirt.engine.ui.webadmin.section.main.view.tab.virtualMachine;

import java.util.Date;

import org.ovirt.engine.core.common.businessentities.DiskImage;
import org.ovirt.engine.core.common.businessentities.VM;
import org.ovirt.engine.core.common.businessentities.VolumeType;
import org.ovirt.engine.ui.uicommonweb.UICommand;
import org.ovirt.engine.ui.uicommonweb.models.vms.VmDiskListModel;
import org.ovirt.engine.ui.uicommonweb.models.vms.VmListModel;
import org.ovirt.engine.ui.webadmin.section.main.presenter.tab.virtualMachine.SubTabVirtualMachineVirtualDiskPresenter;
import org.ovirt.engine.ui.webadmin.section.main.view.AbstractSubTabTableView;
import org.ovirt.engine.ui.webadmin.uicommon.model.SearchableDetailModelProvider;
import org.ovirt.engine.ui.webadmin.widget.table.UiCommandButtonDefinition;
import org.ovirt.engine.ui.webadmin.widget.table.column.DiskSizeColumn;
import org.ovirt.engine.ui.webadmin.widget.table.column.EnumColumn;
import org.ovirt.engine.ui.webadmin.widget.table.column.FullDateTimeColumn;

import com.google.gwt.user.cellview.client.TextColumn;
import com.google.inject.Inject;

public class SubTabVirtualMachineVirtualDiskView extends AbstractSubTabTableView<VM, DiskImage, VmListModel, VmDiskListModel> implements SubTabVirtualMachineVirtualDiskPresenter.ViewDef {

    @Inject
    public SubTabVirtualMachineVirtualDiskView(SearchableDetailModelProvider<DiskImage, VmListModel, VmDiskListModel> modelProvider) {
        super(modelProvider);
        initTable();
        initWidget(getTable());
    }

    private void initTable() {
        TextColumn<DiskImage> nameColumn = new TextColumn<DiskImage>() {
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

        TextColumn<DiskImage> typeColumn = new TextColumn<DiskImage>() {
            @Override
            public String getValue(DiskImage object) {
                return object.getdisk_type().toString();
            }
        };
        getTable().addColumn(typeColumn, "Type");

        TextColumn<DiskImage> formatColumn = new TextColumn<DiskImage>() {
            @Override
            public String getValue(DiskImage object) {
                return object.getvolume_format().toString();
            }
        };
        getTable().addColumn(formatColumn, "Format");

        TextColumn<DiskImage> allocationColumn = new EnumColumn<DiskImage, VolumeType>() {
            @Override
            protected VolumeType getRawValue(DiskImage object) {
                return VolumeType.forValue(object.getvolume_type().getValue());
            }
        };
        getTable().addColumn(allocationColumn, "Allocation");

        TextColumn<DiskImage> interfaceColumn = new TextColumn<DiskImage>() {
            @Override
            public String getValue(DiskImage object) {
                return object.getdisk_interface().toString();
            }
        };
        getTable().addColumn(interfaceColumn, "Interface");

        TextColumn<DiskImage> dateCreatedColumn = new FullDateTimeColumn<DiskImage>() {
            @Override
            protected Date getRawValue(DiskImage object) {
                return object.getcreation_date();
            }
        };
        getTable().addColumn(dateCreatedColumn, "Date Created");

        getTable().addActionButton(new UiCommandButtonDefinition<DiskImage>("New") {
            @Override
            protected UICommand resolveCommand() {
                return getDetailModel().getNewCommand();
            }
        });

        getTable().addActionButton(new UiCommandButtonDefinition<DiskImage>("Edit") {
            @Override
            protected UICommand resolveCommand() {
                return getDetailModel().getEditCommand();
            }
        });

        getTable().addActionButton(new UiCommandButtonDefinition<DiskImage>("Remove") {
            @Override
            protected UICommand resolveCommand() {
                return getDetailModel().getRemoveCommand();
            }
        });
    }

}
