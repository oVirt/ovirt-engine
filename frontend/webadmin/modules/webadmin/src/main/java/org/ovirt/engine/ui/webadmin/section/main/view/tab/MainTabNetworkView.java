package org.ovirt.engine.ui.webadmin.section.main.view.tab;

import org.ovirt.engine.core.common.businessentities.NetworkView;
import org.ovirt.engine.ui.common.idhandler.ElementIdHandler;
import org.ovirt.engine.ui.common.uicommon.model.MainModelProvider;
import org.ovirt.engine.ui.common.widget.table.column.TextColumnWithTooltip;
import org.ovirt.engine.ui.uicommonweb.UICommand;
import org.ovirt.engine.ui.uicommonweb.models.networks.NetworkListModel;
import org.ovirt.engine.ui.webadmin.ApplicationConstants;
import org.ovirt.engine.ui.webadmin.section.main.presenter.tab.MainTabNetworkPresenter;
import org.ovirt.engine.ui.webadmin.section.main.view.AbstractMainTabWithDetailsTableView;
import org.ovirt.engine.ui.webadmin.widget.action.WebAdminButtonDefinition;
import org.ovirt.engine.ui.webadmin.widget.table.column.NetworkRoleColumn;

import com.google.gwt.core.client.GWT;
import com.google.inject.Inject;

public class MainTabNetworkView extends AbstractMainTabWithDetailsTableView<NetworkView, NetworkListModel> implements MainTabNetworkPresenter.ViewDef {

    interface ViewIdHandler extends ElementIdHandler<MainTabNetworkView> {

        ViewIdHandler idHandler = GWT.create(ViewIdHandler.class);
    }

    ApplicationConstants constants;

    @Inject
    public MainTabNetworkView(MainModelProvider<NetworkView, NetworkListModel> modelProvider, ApplicationConstants constants) {
        super(modelProvider);
        ViewIdHandler.idHandler.generateAndSetIds(this);
        initTable(constants);
        initWidget(getTable());
    }


    void initTable(final ApplicationConstants constants) {

        TextColumnWithTooltip<NetworkView> nameColumn = new TextColumnWithTooltip<NetworkView>() {
            @Override
            public String getValue(NetworkView object) {
                return object.getNetwork().getName();
            }
        };

        getTable().addColumn(nameColumn, constants.nameNetwork());

        TextColumnWithTooltip<NetworkView> dcColumn = new TextColumnWithTooltip<NetworkView>() {
            @Override
            public String getValue(NetworkView object) {
                return object.getStoragePoolName();
            }
        };

        getTable().addColumn(dcColumn, constants.dcNetwork());

        TextColumnWithTooltip<NetworkView> descriptionColumn = new TextColumnWithTooltip<NetworkView>(40) {
            @Override
            public String getValue(NetworkView object) {
                return object.getNetwork().getdescription();
            }
        };
        getTable().addColumn(descriptionColumn, constants.descriptionNetwork());

        NetworkRoleColumn roleColumn = new NetworkRoleColumn();

        getTable().addColumn(roleColumn, constants.roleNetwork(), "30px"); //$NON-NLS-1$

        TextColumnWithTooltip<NetworkView> vlanColumn = new TextColumnWithTooltip<NetworkView>() {
            @Override
            public String getValue(NetworkView object) {
                return object.getNetwork().getvlan_id()== null ? "-" : object.getNetwork().getvlan_id().toString(); //$NON-NLS-1$
            }
        };
        getTable().addColumn(vlanColumn, constants.vlanNetwork());

        getTable().addActionButton(new WebAdminButtonDefinition<NetworkView>(constants.newNetwork()) {
            @Override
            protected UICommand resolveCommand() {
                return getMainModel().getNewCommand();
            }
        });
        getTable().addActionButton(new WebAdminButtonDefinition<NetworkView>(constants.editNetwork()) {
            @Override
            protected UICommand resolveCommand() {
                return getMainModel().getEditCommand();
            }
        });
        getTable().addActionButton(new WebAdminButtonDefinition<NetworkView>(constants.removeNetwork()) {
            @Override
            protected UICommand resolveCommand() {
                return getMainModel().getRemoveCommand();
            }
        });

    }
}

