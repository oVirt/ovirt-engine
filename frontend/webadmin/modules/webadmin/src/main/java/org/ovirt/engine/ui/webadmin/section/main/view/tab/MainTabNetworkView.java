package org.ovirt.engine.ui.webadmin.section.main.view.tab;

import org.ovirt.engine.core.common.businessentities.Network;
import org.ovirt.engine.ui.common.idhandler.ElementIdHandler;
import org.ovirt.engine.ui.common.uicommon.model.MainModelProvider;
import org.ovirt.engine.ui.common.widget.table.column.TextColumnWithTooltip;
import org.ovirt.engine.ui.uicommonweb.UICommand;
import org.ovirt.engine.ui.uicommonweb.models.networks.NetworkListModel;
import org.ovirt.engine.ui.webadmin.ApplicationConstants;
import org.ovirt.engine.ui.webadmin.section.main.presenter.tab.MainTabNetworkPresenter;
import org.ovirt.engine.ui.webadmin.section.main.view.AbstractMainTabWithDetailsTableView;
import org.ovirt.engine.ui.webadmin.widget.action.WebAdminButtonDefinition;

import com.google.gwt.core.client.GWT;
import com.google.inject.Inject;

public class MainTabNetworkView extends AbstractMainTabWithDetailsTableView<Network, NetworkListModel> implements MainTabNetworkPresenter.ViewDef {

    interface ViewIdHandler extends ElementIdHandler<MainTabNetworkView> {

        ViewIdHandler idHandler = GWT.create(ViewIdHandler.class);
    }

    ApplicationConstants constants;

    @Inject
    public MainTabNetworkView(MainModelProvider<Network, NetworkListModel> modelProvider, ApplicationConstants constants) {
        super(modelProvider);
        ViewIdHandler.idHandler.generateAndSetIds(this);
        initTable(constants);
        initWidget(getTable());
    }


    void initTable(ApplicationConstants constants) {

        TextColumnWithTooltip<Network> nameColumn = new TextColumnWithTooltip<Network>() {
            @Override
            public String getValue(Network object) {
                return object.getName();
            }
        };
        getTable().addColumn(nameColumn, constants.nameNetwork());

        TextColumnWithTooltip<Network> descriptionColumn = new TextColumnWithTooltip<Network>() {
            @Override
            public String getValue(Network object) {
                return object.getdescription();
            }
        };
        getTable().addColumn(descriptionColumn, constants.descriptionNetwork());



        getTable().addActionButton(new WebAdminButtonDefinition<Network>(constants.newNetwork()) {
            @Override
            protected UICommand resolveCommand() {
                return getMainModel().getNewCommand();
            }
        });
        getTable().addActionButton(new WebAdminButtonDefinition<Network>(constants.editNetwork()) {
            @Override
            protected UICommand resolveCommand() {
                return getMainModel().getEditCommand();
            }
        });
        getTable().addActionButton(new WebAdminButtonDefinition<Network>(constants.removeNetwork()) {
            @Override
            protected UICommand resolveCommand() {
                return getMainModel().getRemoveCommand();
            }
        });

    }
}

