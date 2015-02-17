package org.ovirt.engine.ui.webadmin.section.main.view.tab.cluster;

import javax.inject.Inject;

import org.ovirt.engine.core.common.businessentities.VDS;
import org.ovirt.engine.core.common.businessentities.VDSGroup;
import org.ovirt.engine.core.common.businessentities.VDSStatus;
import org.ovirt.engine.core.common.mode.ApplicationMode;
import org.ovirt.engine.ui.common.idhandler.ElementIdHandler;
import org.ovirt.engine.ui.common.uicommon.model.SearchableDetailModelProvider;
import org.ovirt.engine.ui.common.widget.table.column.AbstractEnumColumn;
import org.ovirt.engine.ui.common.widget.table.column.AbstractTextColumnWithTooltip;
import org.ovirt.engine.ui.uicommonweb.UICommand;
import org.ovirt.engine.ui.uicommonweb.models.ApplicationModeHelper;
import org.ovirt.engine.ui.uicommonweb.models.clusters.ClusterHostListModel;
import org.ovirt.engine.ui.uicommonweb.models.clusters.ClusterListModel;
import org.ovirt.engine.ui.uicompat.ConstantsManager;
import org.ovirt.engine.ui.webadmin.ApplicationConstants;
import org.ovirt.engine.ui.webadmin.section.main.presenter.tab.cluster.SubTabClusterHostPresenter;
import org.ovirt.engine.ui.webadmin.section.main.view.AbstractSubTabTableView;
import org.ovirt.engine.ui.webadmin.widget.action.WebAdminButtonDefinition;
import org.ovirt.engine.ui.webadmin.widget.table.column.HostStatusColumn;

import com.google.gwt.core.client.GWT;

public class SubTabClusterHostView extends AbstractSubTabTableView<VDSGroup, VDS, ClusterListModel<Void>, ClusterHostListModel>
        implements SubTabClusterHostPresenter.ViewDef {

    interface ViewIdHandler extends ElementIdHandler<SubTabClusterHostView> {
        ViewIdHandler idHandler = GWT.create(ViewIdHandler.class);
    }

    @Inject
    public SubTabClusterHostView(SearchableDetailModelProvider<VDS, ClusterListModel<Void>, ClusterHostListModel> modelProvider, ApplicationConstants constants) {
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

        getTable().addColumn(new HostStatusColumn(), constants.empty(), "30px"); //$NON-NLS-1$

        AbstractTextColumnWithTooltip<VDS> nameColumn = new AbstractTextColumnWithTooltip<VDS>() {
            @Override
            public String getValue(VDS object) {
                return object.getName();
            }
        };
        nameColumn.makeSortable();
        getTable().addColumn(nameColumn, constants.nameClusterHost(), "220px"); //$NON-NLS-1$

        AbstractTextColumnWithTooltip<VDS> hostColumn = new AbstractTextColumnWithTooltip<VDS>() {
            @Override
            public String getValue(VDS object) {
                return object.getHostName();
            }
        };
        hostColumn.makeSortable();
        getTable().addColumn(hostColumn, constants.hostIpClusterHost(), "220px"); //$NON-NLS-1$

        AbstractTextColumnWithTooltip<VDS> statusColumn = new AbstractEnumColumn<VDS, VDSStatus>() {
            @Override
            public VDSStatus getRawValue(VDS object) {
                return object.getStatus();
            }
        };
        statusColumn.makeSortable();
        getTable().addColumn(statusColumn, constants.statusClusterHost(), "120px"); //$NON-NLS-1$

        if (ApplicationModeHelper.isModeSupported(ApplicationMode.VirtOnly)) {
            AbstractTextColumnWithTooltip<VDS> loadColumn = new AbstractTextColumnWithTooltip<VDS>() {
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
            AbstractTextColumnWithTooltip<VDS> consoleColumn = new AbstractTextColumnWithTooltip<VDS>() {
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
