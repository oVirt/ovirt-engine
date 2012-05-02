package org.ovirt.engine.ui.webadmin.section.main.view.tab;

import java.util.Date;

import org.ovirt.engine.core.common.businessentities.DiskImage;
import org.ovirt.engine.core.common.businessentities.ImageStatus;
import org.ovirt.engine.core.common.businessentities.VolumeType;
import org.ovirt.engine.ui.common.idhandler.ElementIdHandler;
import org.ovirt.engine.ui.common.uicommon.model.MainModelProvider;
import org.ovirt.engine.ui.common.widget.table.column.DiskSizeColumn;
import org.ovirt.engine.ui.common.widget.table.column.EnumColumn;
import org.ovirt.engine.ui.common.widget.table.column.FullDateTimeColumn;
import org.ovirt.engine.ui.common.widget.table.column.ImageResourceColumn;
import org.ovirt.engine.ui.common.widget.table.column.TextColumnWithTooltip;
import org.ovirt.engine.ui.uicommonweb.UICommand;
import org.ovirt.engine.ui.uicommonweb.models.disks.DiskListModel;
import org.ovirt.engine.ui.webadmin.ApplicationConstants;
import org.ovirt.engine.ui.webadmin.ApplicationResources;
import org.ovirt.engine.ui.webadmin.section.main.presenter.tab.MainTabDiskPresenter;
import org.ovirt.engine.ui.webadmin.section.main.view.AbstractMainTabWithDetailsTableView;
import org.ovirt.engine.ui.webadmin.widget.action.WebAdminButtonDefinition;

import com.google.gwt.core.client.GWT;
import com.google.gwt.resources.client.ImageResource;
import com.google.inject.Inject;

public class MainTabDiskView extends AbstractMainTabWithDetailsTableView<DiskImage, DiskListModel> implements MainTabDiskPresenter.ViewDef {

    interface ViewIdHandler extends ElementIdHandler<MainTabDiskView> {
        ViewIdHandler idHandler = GWT.create(ViewIdHandler.class);
    }

    @Inject
    public MainTabDiskView(MainModelProvider<DiskImage, DiskListModel> modelProvider, ApplicationResources resources, ApplicationConstants constants) {
        super(modelProvider);
        ViewIdHandler.idHandler.generateAndSetIds(this);
        initTable(resources, constants);
        initWidget(getTable());
    }

    void initTable(final ApplicationResources resources, ApplicationConstants constants) {
        getTable().addColumn(new TextColumnWithTooltip<DiskImage>() {
            @Override
            public String getValue(DiskImage object) {
                return object.getDiskAlias();
            }
        }, constants.aliasDisk());

        getTable().addColumn(new TextColumnWithTooltip<DiskImage>() {
            @Override
            public String getValue(DiskImage object) {
                return object.getId().toString();
            }
        }, constants.idDisk(), "120px"); //$NON-NLS-1$

        getTable().addColumn(new ImageResourceColumn<DiskImage>() {
            @Override
            public ImageResource getValue(DiskImage object) {
                return object.isBoot() ? resources.bootableDiskIcon() : null;
            }
        }, constants.bootableDisk(), "60px"); //$NON-NLS-1$

        DiskSizeColumn<DiskImage> sizeColumn = new DiskSizeColumn<DiskImage>() {
            @Override
            protected Long getRawValue(DiskImage object) {
                return object.getsize();
            }
        };
        getTable().addColumn(sizeColumn, constants.provisionedSizeDisk(), "100px"); //$NON-NLS-1$

        DiskSizeColumn<DiskImage> actualSizeColumn = new DiskSizeColumn<DiskImage>() {
            @Override
            protected Long getRawValue(DiskImage object) {
                return object.getactual_size();
            }
        };
        getTable().addColumn(actualSizeColumn, constants.sizeDisk(), "100px"); //$NON-NLS-1$

        TextColumnWithTooltip<DiskImage> allocationColumn = new EnumColumn<DiskImage, VolumeType>() {
            @Override
            protected VolumeType getRawValue(DiskImage object) {
                return VolumeType.forValue(object.getvolume_type().getValue());
            }
        };
        getTable().addColumn(allocationColumn, constants.allocationDisk(), "100px"); //$NON-NLS-1$

        TextColumnWithTooltip<DiskImage> dateCreatedColumn = new FullDateTimeColumn<DiskImage>() {
            @Override
            protected Date getRawValue(DiskImage object) {
                return object.getcreation_date();
            }
        };
        getTable().addColumn(dateCreatedColumn, constants.creationDateDisk(), "160px"); //$NON-NLS-1$

        TextColumnWithTooltip<DiskImage> statusColumn = new EnumColumn<DiskImage, ImageStatus>() {
            @Override
            protected ImageStatus getRawValue(DiskImage object) {
                return object.getimageStatus();
            }
        };
        getTable().addColumn(statusColumn, constants.statusDisk(), "100px"); //$NON-NLS-1$

        getTable().addColumn(new TextColumnWithTooltip<DiskImage>() {
            @Override
            public String getValue(DiskImage object) {
                return object.getDiskDescription();
            }
        }, constants.descriptionDisk());

        getTable().addActionButton(new WebAdminButtonDefinition<DiskImage>(constants.addDisk()) {
            @Override
            protected UICommand resolveCommand() {
                return getMainModel().getNewCommand();
            }
        });

        getTable().addActionButton(new WebAdminButtonDefinition<DiskImage>(constants.removeDisk()) {
            @Override
            protected UICommand resolveCommand() {
                return getMainModel().getRemoveCommand();
            }
        });

        getTable().addActionButton(new WebAdminButtonDefinition<DiskImage>(constants.moveDisk()) {
            @Override
            protected UICommand resolveCommand() {
                return getMainModel().getMoveCommand();
            }
        });

        getTable().addActionButton(new WebAdminButtonDefinition<DiskImage>(constants.copyDisk()) {
            @Override
            protected UICommand resolveCommand() {
                return getMainModel().getCopyCommand();
            }
        });
    }

}
