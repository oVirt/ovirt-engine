package org.ovirt.engine.ui.webadmin.section.main.view.tab.disk;

import javax.inject.Inject;

import org.ovirt.engine.core.common.businessentities.Disk;
import org.ovirt.engine.core.common.businessentities.VM;
import org.ovirt.engine.core.common.businessentities.VMStatus;
import org.ovirt.engine.ui.common.idhandler.ElementIdHandler;
import org.ovirt.engine.ui.common.uicommon.model.SearchableDetailModelProvider;
import org.ovirt.engine.ui.common.widget.table.column.EnumColumn;
import org.ovirt.engine.ui.common.widget.table.column.ImageResourceColumn;
import org.ovirt.engine.ui.common.widget.table.column.TextColumnWithTooltip;
import org.ovirt.engine.ui.uicommonweb.models.disks.DiskListModel;
import org.ovirt.engine.ui.uicommonweb.models.disks.DiskVmListModel;
import org.ovirt.engine.ui.webadmin.ApplicationConstants;
import org.ovirt.engine.ui.webadmin.section.main.presenter.tab.disk.SubTabDiskVmPresenter;
import org.ovirt.engine.ui.webadmin.section.main.view.AbstractSubTabTableView;
import org.ovirt.engine.ui.webadmin.widget.table.column.PercentColumn;
import org.ovirt.engine.ui.webadmin.widget.table.column.UptimeColumn;
import org.ovirt.engine.ui.webadmin.widget.table.column.VmTypeColumn;

import com.google.gwt.core.client.GWT;
import com.google.gwt.resources.client.ImageResource;

public class SubTabDiskVmView extends AbstractSubTabTableView<Disk, VM, DiskListModel, DiskVmListModel>
        implements SubTabDiskVmPresenter.ViewDef {

    interface ViewIdHandler extends ElementIdHandler<SubTabDiskVmView> {
        ViewIdHandler idHandler = GWT.create(ViewIdHandler.class);
    }

    @Inject
    public SubTabDiskVmView(SearchableDetailModelProvider<VM, DiskListModel, DiskVmListModel> modelProvider, ApplicationConstants constants) {
        super(modelProvider);
        initTable(constants);
        initWidget(getTable());
    }

    @Override
    protected void generateIds() {
        ViewIdHandler.idHandler.generateAndSetIds(this);
    }

    void initTable(final ApplicationConstants constants) {
        getTable().enableColumnResizing();

        ImageResourceColumn<VM> pluggedColumn = new ImageResourceColumn<VM>() {
            @Override
            public ImageResource getValue(VM object) {
                boolean isDiskPlugged = getDetailModel().isDiskPluggedToVm(object);
                setTitle(isDiskPlugged ? constants.active() : constants.inactive());
                return isDiskPlugged ? getCommonResources().upImage() : getCommonResources().downImage();
            }
        };
        getTable().addColumn(pluggedColumn, constants.empty(), "30px"); //$NON-NLS-1$

        TextColumnWithTooltip<VM> nameColumn = new TextColumnWithTooltip<VM>() {
            @Override
            public String getValue(VM object) {
                return object.getName();
            }
        };
        nameColumn.makeSortable();
        getTable().addColumn(nameColumn, constants.nameVm(), "140px"); //$NON-NLS-1$

        getTable().addColumn(new VmTypeColumn(), constants.empty(), "30px"); //$NON-NLS-1$

        TextColumnWithTooltip<VM> clusterColumn = new TextColumnWithTooltip<VM>() {
            @Override
            public String getValue(VM object) {
                return object.getVdsGroupName();
            }
        };
        clusterColumn.makeSortable();
        getTable().addColumn(clusterColumn, constants.clusterVm(), "140px"); //$NON-NLS-1$

        TextColumnWithTooltip<VM> ipColumn = new TextColumnWithTooltip<VM>() {
            @Override
            public String getValue(VM object) {
                return object.getVmIp();
            }
        };
        ipColumn.makeSortable();
        getTable().addColumn(ipColumn, constants.ipVm(), "140px"); //$NON-NLS-1$

        TextColumnWithTooltip<VM> fqdnColumn = new TextColumnWithTooltip<VM>() {
            @Override
            public String getValue(VM object) {
                return object.getVmFQDN();
            }
        };
        fqdnColumn.makeSortable();
        getTable().addColumn(fqdnColumn, constants.fqdn(), "140px"); //$NON-NLS-1$

        PercentColumn<VM> memColumn = new PercentColumn<VM>() {
            @Override
            public Integer getProgressValue(VM object) {
                return object.getUsageMemPercent();
            }
        };
        getTable().addColumn(memColumn, constants.memoryVm(), "140px"); //$NON-NLS-1$

        PercentColumn<VM> cpuColumn = new PercentColumn<VM>() {
            @Override
            public Integer getProgressValue(VM object) {
                return object.getUsageCpuPercent();
            }
        };
        getTable().addColumn(cpuColumn, constants.cpuVm(), "140px"); //$NON-NLS-1$

        PercentColumn<VM> netColumn = new PercentColumn<VM>() {
            @Override
            public Integer getProgressValue(VM object) {
                return object.getUsageNetworkPercent();
            }
        };
        getTable().addColumn(netColumn, constants.networkVm(), "140px"); //$NON-NLS-1$

        TextColumnWithTooltip<VM> statusColumn = new EnumColumn<VM, VMStatus>() {
            @Override
            protected VMStatus getRawValue(VM object) {
                return object.getStatus();
            }
        };
        statusColumn.makeSortable();
        getTable().addColumn(statusColumn, constants.statusVm(), "140px"); //$NON-NLS-1$

        TextColumnWithTooltip<VM> hostColumn = new UptimeColumn<VM>() {
            @Override
            protected Double getRawValue(VM object) {
                return object.getRoundedElapsedTime();
            }
        };
        hostColumn.makeSortable();
        getTable().addColumn(hostColumn, constants.uptimeVm(), "140px"); //$NON-NLS-1$
    }

}
