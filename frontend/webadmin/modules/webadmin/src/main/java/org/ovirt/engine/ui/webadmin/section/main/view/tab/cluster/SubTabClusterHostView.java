package org.ovirt.engine.ui.webadmin.section.main.view.tab.cluster;

import javax.inject.Inject;

import org.ovirt.engine.core.common.businessentities.Cluster;
import org.ovirt.engine.core.common.businessentities.VDS;
import org.ovirt.engine.core.common.businessentities.VDSStatus;
import org.ovirt.engine.core.common.mode.ApplicationMode;
import org.ovirt.engine.ui.common.idhandler.ElementIdHandler;
import org.ovirt.engine.ui.common.uicommon.model.SearchableDetailModelProvider;
import org.ovirt.engine.ui.common.widget.table.column.AbstractEnumColumn;
import org.ovirt.engine.ui.common.widget.table.column.AbstractTextColumn;
import org.ovirt.engine.ui.uicommonweb.UICommand;
import org.ovirt.engine.ui.uicommonweb.models.ApplicationModeHelper;
import org.ovirt.engine.ui.uicommonweb.models.clusters.ClusterHostListModel;
import org.ovirt.engine.ui.uicommonweb.models.clusters.ClusterListModel;
import org.ovirt.engine.ui.uicompat.ConstantsManager;
import org.ovirt.engine.ui.webadmin.ApplicationConstants;
import org.ovirt.engine.ui.webadmin.gin.AssetProvider;
import org.ovirt.engine.ui.webadmin.section.main.presenter.tab.cluster.SubTabClusterHostPresenter;
import org.ovirt.engine.ui.webadmin.section.main.view.AbstractSubTabTableView;
import org.ovirt.engine.ui.webadmin.widget.action.WebAdminButtonDefinition;
import org.ovirt.engine.ui.webadmin.widget.table.column.HostStatusColumn;
import com.google.gwt.core.client.GWT;

public class SubTabClusterHostView extends AbstractSubTabTableView<Cluster, VDS, ClusterListModel<Void>, ClusterHostListModel>
        implements SubTabClusterHostPresenter.ViewDef {

    interface ViewIdHandler extends ElementIdHandler<SubTabClusterHostView> {
        ViewIdHandler idHandler = GWT.create(ViewIdHandler.class);
    }

    private static final ApplicationConstants constants = AssetProvider.getConstants();

    @Inject
    public SubTabClusterHostView(SearchableDetailModelProvider<VDS, ClusterListModel<Void>, ClusterHostListModel> modelProvider) {
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

        HostStatusColumn statusIconColumn = new HostStatusColumn();
        statusIconColumn.setContextMenuTitle(constants.statusIconClusterHost());
        getTable().addColumn(statusIconColumn, constants.empty(), "30px"); //$NON-NLS-1$

        AbstractTextColumn<VDS> nameColumn = new AbstractTextColumn<VDS>() {
            @Override
            public String getValue(VDS object) {
                return object.getName();
            }
        };
        nameColumn.makeSortable();
        getTable().addColumn(nameColumn, constants.nameClusterHost(), "220px"); //$NON-NLS-1$

        AbstractTextColumn<VDS> hostColumn = new AbstractTextColumn<VDS>() {
            @Override
            public String getValue(VDS object) {
                return object.getHostName();
            }
        };
        hostColumn.makeSortable();
        getTable().addColumn(hostColumn, constants.hostIpClusterHost(), "220px"); //$NON-NLS-1$

        AbstractTextColumn<VDS> statusColumn = new AbstractEnumColumn<VDS, VDSStatus>() {
            @Override
            public VDSStatus getRawValue(VDS object) {
                return object.getStatus();
            }
        };
        statusColumn.makeSortable();
        getTable().addColumn(statusColumn, constants.statusClusterHost(), "120px"); //$NON-NLS-1$

        if (ApplicationModeHelper.isModeSupported(ApplicationMode.VirtOnly)) {
            AbstractTextColumn<VDS> loadColumn = new AbstractTextColumn<VDS>() {
                @Override
                public String getValue(VDS object) {
                    int numOfActiveVMs = object.getVmActive() != null ? object.getVmActive() : 0;
                    return ConstantsManager.getInstance().getMessages().numberOfVmsForHostsLoad(numOfActiveVMs);
                }
            };
            loadColumn.makeSortable();
            getTable().addColumn(loadColumn, constants.loadClusterHost(), "120px"); //$NON-NLS-1$
        }

        if (ApplicationModeHelper.isModeSupported(ApplicationMode.VirtOnly)) {
            AbstractTextColumn<VDS> consoleColumn = new AbstractTextColumn<VDS>() {
                @Override
                public String getValue(VDS host) {
                    return host.getConsoleAddress() != null ? constants.yes() : constants.no();
                }

            };
            consoleColumn.makeSortable();
            getTable().addColumn(consoleColumn, constants.overriddenConsoleAddress(), "220px"); //$NON-NLS-1$
        }

        getTable().addActionButton(new WebAdminButtonDefinition<VDS>(constants.updateMomPolicyClusterHost()) {

            @Override
            protected UICommand resolveCommand() {
                return getDetailModel().getUpdateMomPolicyCommand();
            }
        });
    }
}
