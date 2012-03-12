package org.ovirt.engine.ui.common.widget.uicommon.template;

import org.ovirt.engine.core.common.businessentities.DiskInterface;
import org.ovirt.engine.core.common.businessentities.DiskType;
import org.ovirt.engine.core.common.businessentities.VolumeFormat;
import org.ovirt.engine.core.common.businessentities.VolumeType;
import org.ovirt.engine.ui.common.system.ClientStorage;
import org.ovirt.engine.ui.common.uicommon.model.SearchableTableModelProvider;
import org.ovirt.engine.ui.common.widget.table.column.EnumColumn;
import org.ovirt.engine.ui.common.widget.table.column.TextColumnWithTooltip;
import org.ovirt.engine.ui.common.widget.uicommon.AbstractModelBoundTableWidget;
import org.ovirt.engine.ui.uicommonweb.models.templates.TemplateDiskListModel;
import org.ovirt.engine.ui.uicommonweb.models.vms.DiskModel;

import com.google.gwt.event.shared.EventBus;

public class TemplateDiskListModelTable extends AbstractModelBoundTableWidget<DiskModel, TemplateDiskListModel> {

    public TemplateDiskListModelTable(
            SearchableTableModelProvider<DiskModel, TemplateDiskListModel> modelProvider,
            EventBus eventBus, ClientStorage clientStorage) {
        super(modelProvider, eventBus, clientStorage, false);
    }

    @Override
    public void initTable() {
        TextColumnWithTooltip<DiskModel> nameColumn = new TextColumnWithTooltip<DiskModel>() {
            @Override
            public String getValue(DiskModel object) {
                return "Disk " + object.getDiskImage().getinternal_drive_mapping();
            }
        };
        getTable().addColumn(nameColumn, "Name");

        TextColumnWithTooltip<DiskModel> sizeColumn = new TextColumnWithTooltip<DiskModel>() {
            @Override
            public String getValue(DiskModel object) {
                return String.valueOf(object.getDiskImage().getSizeInGigabytes()) + " GB";
            }
        };
        getTable().addColumn(sizeColumn, "Size");

        TextColumnWithTooltip<DiskModel> typeColumn = new EnumColumn<DiskModel, DiskType>() {
            @Override
            protected DiskType getRawValue(DiskModel object) {
                return object.getDiskImage().getdisk_type();
            }
        };
        getTable().addColumn(typeColumn, "Type");

        TextColumnWithTooltip<DiskModel> formatColumn = new EnumColumn<DiskModel, VolumeFormat>() {
            @Override
            protected VolumeFormat getRawValue(DiskModel object) {
                return object.getDiskImage().getvolume_format();
            }
        };
        getTable().addColumn(formatColumn, "Format");

        TextColumnWithTooltip<DiskModel> allocationColumn = new EnumColumn<DiskModel, VolumeType>() {
            @Override
            protected VolumeType getRawValue(DiskModel object) {
                return VolumeType.forValue(object.getDiskImage().getvolume_type().getValue());
            }
        };
        getTable().addColumn(allocationColumn, "Allocation");

        TextColumnWithTooltip<DiskModel> interfaceColumn = new EnumColumn<DiskModel, DiskInterface>() {
            @Override
            protected DiskInterface getRawValue(DiskModel object) {
                return object.getDiskImage().getdisk_interface();
            }
        };
        getTable().addColumn(interfaceColumn, "Interface");
    }

}
