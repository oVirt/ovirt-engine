package org.ovirt.engine.ui.webadmin.section.main.view.tab.provider;

import javax.inject.Inject;

import org.ovirt.engine.core.common.businessentities.Provider;
import org.ovirt.engine.core.common.businessentities.network.NetworkView;
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

public class SubTabProviderNetworkView extends AbstractSubTabTableView<Provider, NetworkView, ProviderListModel, ProviderNetworkListModel>
        implements SubTabProviderNetworkPresenter.ViewDef {

    private final ApplicationConstants constants;

    @Inject
    public SubTabProviderNetworkView(SearchableDetailModelProvider<NetworkView, ProviderListModel, ProviderNetworkListModel> modelProvider,
            ApplicationConstants constants,
            ApplicationResources resources) {
        super(modelProvider);
        this.constants = constants;
        initTable();
        initWidget(getTable());
    }

    void initTable() {
        getTable().enableColumnResizing();

        TextColumnWithTooltip<NetworkView> nameColumn =
                new TextColumnWithTooltip<NetworkView>() {
                    @Override
                    public String getValue(NetworkView object) {
                        return object.getName();
                    }
                };
        getTable().addColumn(nameColumn, constants.nameNetwork(), "200px"); //$NON-NLS-1$

        TextColumnWithTooltip<NetworkView> externalIdColumn =
                new TextColumnWithTooltip<NetworkView>() {
                    @Override
                    public String getValue(NetworkView object) {
                        return object.getProvidedBy().getExternalId();
                    }
                };
        getTable().addColumn(externalIdColumn, constants.externalIdProviderNetwork(), "300px"); //$NON-NLS-1$

        TextColumnWithTooltip<NetworkView> dcColumn = new TextColumnWithTooltip<NetworkView>() {
            @Override
            public String getValue(NetworkView object) {
                return object.getDataCenterName();
            }
        };
        getTable().addColumn(dcColumn, constants.dataCenterProviderNetwork(), "200px"); //$NON-NLS-1$

        getTable().addActionButton(new WebAdminButtonDefinition<NetworkView>(constants.importNetwork()) {
            @Override
            protected UICommand resolveCommand() {
                return getDetailModel().getDiscoverCommand();
            }
        });
    }

}
