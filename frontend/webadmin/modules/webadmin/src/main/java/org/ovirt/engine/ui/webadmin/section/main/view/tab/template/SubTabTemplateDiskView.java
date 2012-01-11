package org.ovirt.engine.ui.webadmin.section.main.view.tab.template;

import org.ovirt.engine.core.common.businessentities.DiskImage;
import org.ovirt.engine.core.common.businessentities.DiskInterface;
import org.ovirt.engine.core.common.businessentities.DiskType;
import org.ovirt.engine.core.common.businessentities.VmTemplate;
import org.ovirt.engine.core.common.businessentities.VolumeFormat;
import org.ovirt.engine.core.common.businessentities.VolumeType;
import org.ovirt.engine.ui.common.uicommon.model.SearchableDetailModelProvider;
import org.ovirt.engine.ui.common.widget.table.column.EnumColumn;
import org.ovirt.engine.ui.common.widget.table.column.TextColumnWithTooltip;
import org.ovirt.engine.ui.uicommonweb.models.templates.TemplateDiskListModel;
import org.ovirt.engine.ui.uicommonweb.models.templates.TemplateListModel;
import org.ovirt.engine.ui.webadmin.section.main.presenter.tab.template.SubTabTemplateDiskPresenter;
import org.ovirt.engine.ui.webadmin.section.main.view.AbstractSubTabTableView;

import com.google.inject.Inject;

public class SubTabTemplateDiskView extends AbstractSubTabTableView<VmTemplate, DiskImage, TemplateListModel, TemplateDiskListModel>
        implements SubTabTemplateDiskPresenter.ViewDef {

    @Inject
    public SubTabTemplateDiskView(SearchableDetailModelProvider<DiskImage, TemplateListModel, TemplateDiskListModel> modelProvider) {
        super(modelProvider);
        initTable();
        initWidget(getTable());
    }

    void initTable() {
        TextColumnWithTooltip<DiskImage> nameColumn = new TextColumnWithTooltip<DiskImage>() {
            @Override
            public String getValue(DiskImage object) {
                return "Disk " + object.getinternal_drive_mapping();
            }
        };
        getTable().addColumn(nameColumn, "Name");

        TextColumnWithTooltip<DiskImage> sizeColumn = new TextColumnWithTooltip<DiskImage>() {
            @Override
            public String getValue(DiskImage object) {
                return String.valueOf(object.getSizeInGigabytes()) + " GB";
            }
        };
        getTable().addColumn(sizeColumn, "Size");

        TextColumnWithTooltip<DiskImage> typeColumn = new EnumColumn<DiskImage, DiskType>() {
            @Override
            protected DiskType getRawValue(DiskImage object) {
                return object.getdisk_type();
            }
        };
        getTable().addColumn(typeColumn, "Type");

        TextColumnWithTooltip<DiskImage> formatColumn = new EnumColumn<DiskImage, VolumeFormat>() {
            @Override
            protected VolumeFormat getRawValue(DiskImage object) {
                return object.getvolume_format();
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

        TextColumnWithTooltip<DiskImage> interfaceColumn = new EnumColumn<DiskImage, DiskInterface>() {
            @Override
            protected DiskInterface getRawValue(DiskImage object) {
                return object.getdisk_interface();
            }
        };
        getTable().addColumn(interfaceColumn, "Interface");
    }

}
