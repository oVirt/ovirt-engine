package org.ovirt.engine.ui.webadmin.section.main.view.tab.host;

import javax.inject.Inject;

import org.ovirt.engine.core.common.businessentities.VDS;
import org.ovirt.engine.core.common.businessentities.gluster.GlusterServerService;
import org.ovirt.engine.core.common.businessentities.gluster.GlusterServiceStatus;
import org.ovirt.engine.ui.common.uicommon.model.SearchableDetailModelProvider;
import org.ovirt.engine.ui.common.widget.table.column.EnumColumn;
import org.ovirt.engine.ui.common.widget.table.column.TextColumnWithTooltip;
import org.ovirt.engine.ui.uicommonweb.UICommand;
import org.ovirt.engine.ui.uicommonweb.models.gluster.HostGlusterSwiftListModel;
import org.ovirt.engine.ui.uicommonweb.models.hosts.HostListModel;
import org.ovirt.engine.ui.webadmin.ApplicationConstants;
import org.ovirt.engine.ui.webadmin.section.main.presenter.tab.host.SubTabHostGlusterSwiftPresenter;
import org.ovirt.engine.ui.webadmin.section.main.view.AbstractSubTabTableView;
import org.ovirt.engine.ui.webadmin.widget.action.WebAdminButtonDefinition;

public class SubTabHostGlusterSwiftView extends AbstractSubTabTableView<VDS, GlusterServerService, HostListModel, HostGlusterSwiftListModel>
        implements SubTabHostGlusterSwiftPresenter.ViewDef {

    @Inject
    public SubTabHostGlusterSwiftView(SearchableDetailModelProvider<GlusterServerService, HostListModel, HostGlusterSwiftListModel> modelProvider,
            ApplicationConstants constants) {
        super(modelProvider);
        initTable(constants);
        initWidget(getTable());
    }

    void initTable(ApplicationConstants constants) {
        getTable().enableColumnResizing();

        TextColumnWithTooltip<GlusterServerService> serviceColumn = new TextColumnWithTooltip<GlusterServerService>() {
            @Override
            public String getValue(GlusterServerService object) {
                return object.getServiceName();
            }
        };
        serviceColumn.makeSortable();
        getTable().addColumn(serviceColumn, constants.serviceGlusterSwift(), "250px"); //$NON-NLS-1$

        TextColumnWithTooltip<GlusterServerService> statusColumn =
            new EnumColumn<GlusterServerService, GlusterServiceStatus>() {
                @Override
                protected GlusterServiceStatus getRawValue(GlusterServerService object) {
                    return object.getStatus();
                }
            };
        statusColumn.makeSortable();
        getTable().addColumn(statusColumn, constants.statusGlusterSwift(), "250px"); //$NON-NLS-1$

        getTable().addActionButton(new WebAdminButtonDefinition<GlusterServerService>(constants.startGlusterSwiftInHost()) {
            @Override
            protected UICommand resolveCommand() {
                return getDetailModel().getStartSwiftCommand();
            }
        });

        getTable().addActionButton(new WebAdminButtonDefinition<GlusterServerService>(constants.stopGlusterSwiftInHost()) {
            @Override
            protected UICommand resolveCommand() {
                return getDetailModel().getStopSwiftCommand();
            }
        });

        getTable().addActionButton(new WebAdminButtonDefinition<GlusterServerService>(constants.restartGlusterSwiftInHost()) {
            @Override
            protected UICommand resolveCommand() {
                return getDetailModel().getRestartSwiftCommand();
            }
        });
    }

}
