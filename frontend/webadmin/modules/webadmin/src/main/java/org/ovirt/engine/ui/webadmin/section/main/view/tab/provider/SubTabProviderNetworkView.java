package org.ovirt.engine.ui.webadmin.section.main.view.tab.provider;

import javax.inject.Inject;

import org.ovirt.engine.core.common.businessentities.Provider;
import org.ovirt.engine.core.common.businessentities.network.Network;
import org.ovirt.engine.ui.common.uicommon.model.SearchableDetailModelProvider;
import org.ovirt.engine.ui.common.widget.table.column.TextColumnWithTooltip;
import org.ovirt.engine.ui.uicommonweb.UICommand;
import org.ovirt.engine.ui.uicommonweb.models.providers.ProviderListModel;
import org.ovirt.engine.ui.uicommonweb.models.providers.ProviderNetworkListModel;
import org.ovirt.engine.ui.webadmin.ApplicationConstants;
import org.ovirt.engine.ui.webadmin.ApplicationResources;
import org.ovirt.engine.ui.webadmin.section.main.presenter.tab.provider.SubTabProviderNetworkPresenter;
import org.ovirt.engine.ui.webadmin.section.main.view.AbstractSubTabTableView;
import org.ovirt.engine.ui.webadmin.widget.action.WebAdminButtonDefinition;

public class SubTabProviderNetworkView extends AbstractSubTabTableView<Provider, Network, ProviderListModel, ProviderNetworkListModel>
        implements SubTabProviderNetworkPresenter.ViewDef {

    private final ApplicationConstants constants;

    @Inject
    public SubTabProviderNetworkView(SearchableDetailModelProvider<Network, ProviderListModel, ProviderNetworkListModel> modelProvider,
            ApplicationConstants constants,
            ApplicationResources resources) {
        super(modelProvider);
        this.constants = constants;
        initTable();
        initWidget(getTable());
    }

    void initTable() {
        getTable().enableColumnResizing();

        TextColumnWithTooltip<Network> nameColumn =
                new TextColumnWithTooltip<Network>() {
                    @Override
                    public String getValue(Network object) {
                        return object.getName();
                    }
                };
        getTable().addColumn(nameColumn, constants.nameNetwork(), "200px"); //$NON-NLS-1$

        TextColumnWithTooltip<Network> externalIdColumn =
                new TextColumnWithTooltip<Network>() {
                    @Override
                    public String getValue(Network object) {
                        return object.getProvidedBy().getExternalId();
                    }
                };
        getTable().addColumn(externalIdColumn, constants.externalIdProviderNetwork(), "300px"); //$NON-NLS-1$

        getTable().addActionButton(new WebAdminButtonDefinition<Network>(constants.discoverProviderNetwork()) {
            @Override
            protected UICommand resolveCommand() {
                return getDetailModel().getDiscoverCommand();
            }
        });
    }

}
