package org.ovirt.engine.ui.webadmin.section.main.view.tab;

import java.util.Date;

import org.ovirt.engine.core.common.businessentities.Disk;
import org.ovirt.engine.core.common.businessentities.Disk.DiskStorageType;
import org.ovirt.engine.core.common.businessentities.DiskImage;
import org.ovirt.engine.core.common.businessentities.ImageStatus;
import org.ovirt.engine.core.common.businessentities.LunDisk;
import org.ovirt.engine.core.common.businessentities.VmEntityType;
import org.ovirt.engine.core.common.businessentities.VolumeType;
import org.ovirt.engine.ui.common.idhandler.ElementIdHandler;
import org.ovirt.engine.ui.common.uicommon.model.MainModelProvider;
import org.ovirt.engine.ui.common.widget.table.column.DiskContainersColumn;
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

public class MainTabDiskView extends AbstractMainTabWithDetailsTableView<Disk, DiskListModel> implements MainTabDiskPresenter.ViewDef {

    interface ViewIdHandler extends ElementIdHandler<MainTabDiskView> {
        ViewIdHandler idHandler = GWT.create(ViewIdHandler.class);
    }

    @Inject
    public MainTabDiskView(MainModelProvider<Disk, DiskListModel> modelProvider,
            ApplicationResources resources,
            ApplicationConstants constants) {
        super(modelProvider);
        ViewIdHandler.idHandler.generateAndSetIds(this);
        initTable(resources, constants);
        initWidget(getTable());
    }

    void initTable(final ApplicationResources resources, final ApplicationConstants constants) {
        getTable().addColumn(new TextColumnWithTooltip<Disk>() {
            @Override
            public String getValue(Disk object) {
                return object.getDiskAlias();
            }
        }, constants.aliasDisk());

        getTable().addColumn(new TextColumnWithTooltip<Disk>() {
            @Override
            public String getValue(Disk object) {
                return object.getId().toString();
            }
        }, constants.idDisk(), "120px"); //$NON-NLS-1$

        getTable().addColumn(new ImageResourceColumn<Disk>() {
            @Override
            public ImageResource getValue(Disk object) {
                setTitle(object.isBoot() ? constants.bootableDisk() : null);
                return object.isBoot() ? resources.bootableDiskIcon() : null;
            }
        }, "", "40px"); //$NON-NLS-1$ //$NON-NLS-2$

        getTable().addColumn(new ImageResourceColumn<Disk>() {
            @Override
            public ImageResource getValue(Disk object) {
                setTitle(object.isShareable() ? constants.shareable() : null);
                return object.isShareable() ? resources.shareableDiskIcon() : null;
            }
        }, "", "40px"); //$NON-NLS-1$ //$NON-NLS-2$

        getTable().addColumn(new ImageResourceColumn<Disk>() {
            @Override
            public ImageResource getValue(Disk object) {
                setEnumTitle(object.getDiskStorageType());
                return object.getDiskStorageType() == DiskStorageType.LUN ?
                        resources.externalDiskIcon() : null;
            }
        }, "", "40px"); //$NON-NLS-1$ //$NON-NLS-2$

        getTable().addColumn(new ImageResourceColumn<Disk>() {
            @Override
            public ImageResource getValue(Disk object) {
                setEnumTitle(object.getVmEntityType());
                return object.getVmEntityType() == VmEntityType.VM ? resources.vmsImage() :
                        object.getVmEntityType() == VmEntityType.TEMPLATE ? resources.templatesImage() : null;
            }
        }, "", "40px"); //$NON-NLS-1$ //$NON-NLS-2$

        getTable().addColumn(new DiskContainersColumn(), constants.attachedToDisk(), "150px"); //$NON-NLS-1$

        DiskSizeColumn<Disk> sizeColumn = new DiskSizeColumn<Disk>() {
            @Override
            protected Long getRawValue(Disk object) {
                return object.getDiskStorageType() == DiskStorageType.IMAGE ?
                        ((DiskImage) object).getsize() :
                        (long) (((LunDisk) object).getLun().getDeviceSize() * Math.pow(1024, 3));
            }
        };
        getTable().addColumn(sizeColumn, constants.provisionedSizeDisk(), "100px"); //$NON-NLS-1$

        DiskSizeColumn<Disk> actualSizeColumn = new DiskSizeColumn<Disk>() {
            @Override
            protected Long getRawValue(Disk object) {
                return object.getDiskStorageType() == DiskStorageType.IMAGE ?
                        ((DiskImage) object).getactual_size()
                        : (long) (((LunDisk) object).getLun().getDeviceSize() * Math.pow(1024, 3));
            }
        };
        getTable().addColumn(actualSizeColumn, constants.sizeDisk(), "100px"); //$NON-NLS-1$

        TextColumnWithTooltip<Disk> allocationColumn = new EnumColumn<Disk, VolumeType>() {
            @Override
            protected VolumeType getRawValue(Disk object) {
                return object.getDiskStorageType() == DiskStorageType.IMAGE ?
                        ((DiskImage) object).getvolume_type() : null;
            }
        };
        getTable().addColumn(allocationColumn, constants.allocationDisk(), "100px"); //$NON-NLS-1$

        TextColumnWithTooltip<Disk> dateCreatedColumn = new FullDateTimeColumn<Disk>() {
            @Override
            protected Date getRawValue(Disk object) {
                return object.getDiskStorageType() == DiskStorageType.IMAGE ?
                        ((DiskImage) object).getcreation_date() : null;
            }
        };
        getTable().addColumn(dateCreatedColumn, constants.creationDateDisk(), "150px"); //$NON-NLS-1$

        TextColumnWithTooltip<Disk> statusColumn = new EnumColumn<Disk, ImageStatus>() {
            @Override
            protected ImageStatus getRawValue(Disk object) {
                return object.getDiskStorageType() == DiskStorageType.IMAGE ?
                        ((DiskImage) object).getimageStatus() : null;
            }
        };
        getTable().addColumn(statusColumn, constants.statusDisk(), "100px"); //$NON-NLS-1$

        getTable().addColumn(new TextColumnWithTooltip<Disk>() {
            @Override
            public String getValue(Disk object) {
                return object.getDiskDescription();
            }
        }, constants.descriptionDisk());

        getTable().addActionButton(new WebAdminButtonDefinition<Disk>(constants.addDisk()) {
            @Override
            protected UICommand resolveCommand() {
                return getMainModel().getNewCommand();
            }
        });

        getTable().addActionButton(new WebAdminButtonDefinition<Disk>(constants.removeDisk()) {
            @Override
            protected UICommand resolveCommand() {
                return getMainModel().getRemoveCommand();
            }
        });

        getTable().addActionButton(new WebAdminButtonDefinition<Disk>(constants.moveDisk()) {
            @Override
            protected UICommand resolveCommand() {
                return getMainModel().getMoveCommand();
            }
        });

        getTable().addActionButton(new WebAdminButtonDefinition<Disk>(constants.copyDisk()) {
            @Override
            protected UICommand resolveCommand() {
                return getMainModel().getCopyCommand();
            }
        });
    }

}
