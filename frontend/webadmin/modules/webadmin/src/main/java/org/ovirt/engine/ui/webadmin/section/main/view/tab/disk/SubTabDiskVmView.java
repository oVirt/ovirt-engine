package org.ovirt.engine.ui.webadmin.section.main.view.tab.disk;

import javax.inject.Inject;

import org.ovirt.engine.core.common.businessentities.VM;
import org.ovirt.engine.core.common.businessentities.VMStatus;
import org.ovirt.engine.core.common.businessentities.storage.Disk;
import org.ovirt.engine.ui.common.idhandler.ElementIdHandler;
import org.ovirt.engine.ui.common.uicommon.model.SearchableDetailModelProvider;
import org.ovirt.engine.ui.common.widget.table.column.AbstractEnumColumn;
import org.ovirt.engine.ui.common.widget.table.column.AbstractImageResourceColumn;
import org.ovirt.engine.ui.common.widget.table.column.AbstractTextColumn;
import org.ovirt.engine.ui.uicommonweb.models.disks.DiskListModel;
import org.ovirt.engine.ui.uicommonweb.models.disks.DiskVmListModel;
import org.ovirt.engine.ui.webadmin.ApplicationConstants;
import org.ovirt.engine.ui.webadmin.ApplicationResources;
import org.ovirt.engine.ui.webadmin.gin.AssetProvider;
import org.ovirt.engine.ui.webadmin.section.main.presenter.tab.disk.SubTabDiskVmPresenter;
import org.ovirt.engine.ui.webadmin.section.main.view.AbstractSubTabTableView;
import org.ovirt.engine.ui.webadmin.widget.table.column.AbstractPercentColumn;
import org.ovirt.engine.ui.webadmin.widget.table.column.AbstractUptimeColumn;
import org.ovirt.engine.ui.webadmin.widget.table.column.VmTypeColumn;

import com.google.gwt.core.client.GWT;
import com.google.gwt.resources.client.ImageResource;
import com.google.gwt.safehtml.shared.SafeHtml;
import com.google.gwt.safehtml.shared.SafeHtmlUtils;

public class SubTabDiskVmView extends AbstractSubTabTableView<Disk, VM, DiskListModel, DiskVmListModel>
        implements SubTabDiskVmPresenter.ViewDef {

    interface ViewIdHandler extends ElementIdHandler<SubTabDiskVmView> {
        ViewIdHandler idHandler = GWT.create(ViewIdHandler.class);
    }

    private static final ApplicationResources resources = AssetProvider.getResources();
    private static final ApplicationConstants constants = AssetProvider.getConstants();

    @Inject
    public SubTabDiskVmView(SearchableDetailModelProvider<VM, DiskListModel, DiskVmListModel> modelProvider) {
        super(modelProvider);
        initTable();
        initWidget(getTable());
    }

    @Override
    protected void generateIds() {
        ViewIdHandler.idHandler.generateAndSetIds(this);
    }

    void initTable() {
        getTable().enableColumnResizing();

        AbstractImageResourceColumn<VM> pluggedColumn = new AbstractImageResourceColumn<VM>() {
            @Override
            public ImageResource getValue(VM object) {
                boolean isDiskPlugged = getDetailModel().isDiskPluggedToVm(object);
                return isDiskPlugged ? resources.upImage() : resources.downImage();
            }

            @Override
            public SafeHtml getTooltip(VM object) {
                if (getDetailModel().isDiskPluggedToVm(object)) {
                    return SafeHtmlUtils.fromSafeConstant(constants.active());
                }
                return SafeHtmlUtils.fromSafeConstant(constants.inactive());
            }
        };
        pluggedColumn.setContextMenuTitle(constants.pluggedVm());
        getTable().addColumn(pluggedColumn, constants.empty(), "30px"); //$NON-NLS-1$

        AbstractTextColumn<VM> nameColumn = new AbstractTextColumn<VM>() {
            @Override
            public String getValue(VM object) {
                return object.getName();
            }
        };
        nameColumn.makeSortable();
        getTable().addColumn(nameColumn, constants.nameVm(), "140px"); //$NON-NLS-1$

        VmTypeColumn typeColumn = new VmTypeColumn();
        typeColumn.setContextMenuTitle(constants.typeVm());
        getTable().addColumn(typeColumn, constants.empty(), "60px"); //$NON-NLS-1$

        AbstractTextColumn<VM> clusterColumn = new AbstractTextColumn<VM>() {
            @Override
            public String getValue(VM object) {
                return object.getClusterName();
            }
        };
        clusterColumn.makeSortable();
        getTable().addColumn(clusterColumn, constants.clusterVm(), "140px"); //$NON-NLS-1$

        AbstractTextColumn<VM> ipColumn = new AbstractTextColumn<VM>() {
            @Override
            public String getValue(VM object) {
                return object.getIp();
            }
        };
        ipColumn.makeSortable();
        getTable().addColumn(ipColumn, constants.ipVm(), "140px"); //$NON-NLS-1$

        AbstractTextColumn<VM> fqdnColumn = new AbstractTextColumn<VM>() {
            @Override
            public String getValue(VM object) {
                return object.getFqdn();
            }
        };
        fqdnColumn.makeSortable();
        getTable().addColumn(fqdnColumn, constants.fqdn(), "140px"); //$NON-NLS-1$

        AbstractPercentColumn<VM> memColumn = new AbstractPercentColumn<VM>() {
            @Override
            public Integer getProgressValue(VM object) {
                return object.getUsageMemPercent();
            }
        };
        getTable().addColumn(memColumn, constants.memoryVm(), "140px"); //$NON-NLS-1$

        AbstractPercentColumn<VM> cpuColumn = new AbstractPercentColumn<VM>() {
            @Override
            public Integer getProgressValue(VM object) {
                return object.getUsageCpuPercent();
            }
        };
        getTable().addColumn(cpuColumn, constants.cpuVm(), "140px"); //$NON-NLS-1$

        AbstractPercentColumn<VM> netColumn = new AbstractPercentColumn<VM>() {
            @Override
            public Integer getProgressValue(VM object) {
                return object.getUsageNetworkPercent();
            }
        };
        getTable().addColumn(netColumn, constants.networkVm(), "140px"); //$NON-NLS-1$

        AbstractTextColumn<VM> statusColumn = new AbstractEnumColumn<VM, VMStatus>() {
            @Override
            protected VMStatus getRawValue(VM object) {
                return object.getStatus();
            }
        };
        statusColumn.makeSortable();
        getTable().addColumn(statusColumn, constants.statusVm(), "140px"); //$NON-NLS-1$

        AbstractTextColumn<VM> hostColumn = new AbstractUptimeColumn<VM>() {
            @Override
            protected Double getRawValue(VM object) {
                return object.getElapsedTime();
            }
        };
        hostColumn.makeSortable();
        getTable().addColumn(hostColumn, constants.uptimeVm(), "140px"); //$NON-NLS-1$
    }

}
