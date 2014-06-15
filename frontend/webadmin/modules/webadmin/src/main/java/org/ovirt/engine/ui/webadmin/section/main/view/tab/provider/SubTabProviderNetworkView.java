package org.ovirt.engine.ui.webadmin.section.main.view.tab.provider;

import javax.inject.Inject;

import org.ovirt.engine.core.common.businessentities.Provider;
import org.ovirt.engine.core.common.businessentities.network.NetworkView;
import org.ovirt.engine.ui.common.uicommon.model.SearchableDetailModelProvider;
import org.ovirt.engine.ui.common.widget.table.column.LinkColumnWithTooltip;
import org.ovirt.engine.ui.common.widget.table.column.TextColumnWithTooltip;
import org.ovirt.engine.ui.uicommonweb.UICommand;
import org.ovirt.engine.ui.uicommonweb.models.providers.ProviderListModel;
import org.ovirt.engine.ui.uicommonweb.models.providers.ProviderNetworkListModel;
import org.ovirt.engine.ui.webadmin.ApplicationConstants;
import org.ovirt.engine.ui.webadmin.ApplicationResources;
import org.ovirt.engine.ui.webadmin.section.main.presenter.tab.provider.SubTabProviderNetworkPresenter;
import org.ovirt.engine.ui.webadmin.section.main.view.AbstractSubTabTableView;
import org.ovirt.engine.ui.webadmin.widget.action.WebAdminButtonDefinition;

import com.google.gwt.cell.client.FieldUpdater;

public class SubTabProviderNetworkView extends AbstractSubTabTableView<Provider, NetworkView, ProviderListModel, ProviderNetworkListModel>
        implements SubTabProviderNetworkPresenter.ViewDef {

    private final ApplicationConstants constants;

    private LinkColumnWithTooltip<NetworkView> nameColumn;

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

        nameColumn = new LinkColumnWithTooltip<NetworkView>() {
            @Override
            public String getValue(NetworkView object) {
                return object.getName();
            }
        };
        nameColumn.makeSortable();
        getTable().addColumn(nameColumn, constants.nameNetwork(), "200px"); //$NON-NLS-1$

        TextColumnWithTooltip<NetworkView> externalIdColumn =
                new TextColumnWithTooltip<NetworkView>() {
                    @Override
                    public String getValue(NetworkView object) {
                        return object.getProvidedBy().getExternalId();
                    }
                };
        externalIdColumn.makeSortable();
        getTable().addColumn(externalIdColumn, constants.externalIdProviderNetwork(), "300px"); //$NON-NLS-1$

        TextColumnWithTooltip<NetworkView> dcColumn = new TextColumnWithTooltip<NetworkView>() {
            @Override
            public String getValue(NetworkView object) {
                return object.getDataCenterName();
            }
        };
        dcColumn.makeSortable();
        getTable().addColumn(dcColumn, constants.dataCenterProviderNetwork(), "200px"); //$NON-NLS-1$

        getTable().addActionButton(new WebAdminButtonDefinition<NetworkView>(constants.importNetwork()) {
            @Override
            protected UICommand resolveCommand() {
                return getDetailModel().getDiscoverCommand();
            }
        });
    }

    @Override
    public void setNetworkClickHandler(FieldUpdater<NetworkView, String> fieldUpdater) {
        nameColumn.setFieldUpdater(fieldUpdater);
    }

}
