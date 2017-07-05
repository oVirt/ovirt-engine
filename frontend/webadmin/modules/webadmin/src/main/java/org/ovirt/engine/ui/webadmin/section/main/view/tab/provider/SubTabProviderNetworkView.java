package org.ovirt.engine.ui.webadmin.section.main.view.tab.provider;

import javax.inject.Inject;

import org.ovirt.engine.core.common.businessentities.Provider;
import org.ovirt.engine.core.common.businessentities.network.NetworkView;
import org.ovirt.engine.ui.common.idhandler.ElementIdHandler;
import org.ovirt.engine.ui.common.uicommon.model.SearchableDetailModelProvider;
import org.ovirt.engine.ui.common.widget.table.column.AbstractLinkColumn;
import org.ovirt.engine.ui.common.widget.table.column.AbstractTextColumn;
import org.ovirt.engine.ui.uicommonweb.models.providers.ProviderListModel;
import org.ovirt.engine.ui.uicommonweb.models.providers.ProviderNetworkListModel;
import org.ovirt.engine.ui.webadmin.ApplicationConstants;
import org.ovirt.engine.ui.webadmin.gin.AssetProvider;
import org.ovirt.engine.ui.webadmin.section.main.presenter.tab.provider.SubTabProviderNetworkPresenter;
import org.ovirt.engine.ui.webadmin.section.main.view.AbstractSubTabTableView;

import com.google.gwt.cell.client.FieldUpdater;
import com.google.gwt.core.client.GWT;

public class SubTabProviderNetworkView extends AbstractSubTabTableView<Provider, NetworkView, ProviderListModel, ProviderNetworkListModel>
        implements SubTabProviderNetworkPresenter.ViewDef {

    interface ViewIdHandler extends ElementIdHandler<SubTabProviderNetworkView> {
        ViewIdHandler idHandler = GWT.create(ViewIdHandler.class);
    }

    private AbstractLinkColumn<NetworkView> nameColumn;

    private static final ApplicationConstants constants = AssetProvider.getConstants();

    @Inject
    public SubTabProviderNetworkView(SearchableDetailModelProvider<NetworkView, ProviderListModel, ProviderNetworkListModel> modelProvider) {
        super(modelProvider);
        initTable();
        initWidget(getTableContainer());
    }

    @Override
    protected void generateIds() {
        ViewIdHandler.idHandler.generateAndSetIds(this);
    }

    void initTable() {
        getTable().enableColumnResizing();

        nameColumn = new AbstractLinkColumn<NetworkView>() {
            @Override
            public String getValue(NetworkView object) {
                return object.getName();
            }
        };
        nameColumn.makeSortable();
        getTable().addColumn(nameColumn, constants.nameNetwork(), "200px"); //$NON-NLS-1$

        AbstractTextColumn<NetworkView> externalIdColumn =
                new AbstractTextColumn<NetworkView>() {
                    @Override
                    public String getValue(NetworkView object) {
                        return object.getProvidedBy().getExternalId();
                    }
                };
        externalIdColumn.makeSortable();
        getTable().addColumn(externalIdColumn, constants.externalIdProviderNetwork(), "300px"); //$NON-NLS-1$

        AbstractTextColumn<NetworkView> dcColumn = new AbstractTextColumn<NetworkView>() {
            @Override
            public String getValue(NetworkView object) {
                return object.getDataCenterName();
            }
        };
        dcColumn.makeSortable();
        getTable().addColumn(dcColumn, constants.dataCenterProviderNetwork(), "200px"); //$NON-NLS-1$
    }

    @Override
    public void setNetworkClickHandler(FieldUpdater<NetworkView, String> fieldUpdater) {
        nameColumn.setFieldUpdater(fieldUpdater);
    }

}
