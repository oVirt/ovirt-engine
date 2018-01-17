package org.ovirt.engine.ui.webadmin.section.main.view;

import java.util.HashMap;
import java.util.Map;

import org.ovirt.engine.core.common.businessentities.Provider;
import org.ovirt.engine.core.common.businessentities.ProviderType;
import org.ovirt.engine.core.searchbackend.ProviderConditionFieldAutoCompleter;
import org.ovirt.engine.ui.common.idhandler.ElementIdHandler;
import org.ovirt.engine.ui.common.presenter.FragmentParams;
import org.ovirt.engine.ui.common.uicommon.model.MainModelProvider;
import org.ovirt.engine.ui.common.widget.table.column.AbstractEnumColumn;
import org.ovirt.engine.ui.common.widget.table.column.AbstractLinkColumn;
import org.ovirt.engine.ui.common.widget.table.column.AbstractTextColumn;
import org.ovirt.engine.ui.uicommonweb.models.providers.ProviderListModel;
import org.ovirt.engine.ui.uicommonweb.place.WebAdminApplicationPlaces;
import org.ovirt.engine.ui.webadmin.ApplicationConstants;
import org.ovirt.engine.ui.webadmin.gin.AssetProvider;
import org.ovirt.engine.ui.webadmin.section.main.presenter.MainProviderPresenter;

import com.google.gwt.cell.client.FieldUpdater;
import com.google.gwt.core.client.GWT;
import com.google.inject.Inject;

public class MainProviderView extends AbstractMainWithDetailsTableView<Provider, ProviderListModel> implements MainProviderPresenter.ViewDef {

    interface ViewIdHandler extends ElementIdHandler<MainProviderView> {
        ViewIdHandler idHandler = GWT.create(ViewIdHandler.class);
    }

    private static final ApplicationConstants constants = AssetProvider.getConstants();

    @Inject
    public MainProviderView(MainModelProvider<Provider, ProviderListModel> modelProvider) {
        super(modelProvider);
        ViewIdHandler.idHandler.generateAndSetIds(this);
        initTable();
        initWidget(getTable());
    }

    void initTable() {
        getTable().enableColumnResizing();

        AbstractTextColumn<Provider> nameColumn = new AbstractLinkColumn<Provider>(
                new FieldUpdater<Provider, String>() {

            @Override
            public void update(int index, Provider provider, String value) {
                Map<String, String> parameters = new HashMap<>();
                parameters.put(FragmentParams.NAME.getName(), provider.getName());
                //The link was clicked, now fire an event to switch to details.
                getPlaceTransitionHandler().handlePlaceTransition(
                        WebAdminApplicationPlaces.providerGeneralSubTabPlace, parameters);
            }

        }) {
            @Override
            public String getValue(Provider object) {
                return object.getName();
            }
        };
        nameColumn.makeSortable(ProviderConditionFieldAutoCompleter.NAME);

        getTable().addColumn(nameColumn, constants.nameProvider(), "200px"); //$NON-NLS-1$

        AbstractTextColumn<Provider> typeColumn = new AbstractEnumColumn<Provider, ProviderType>() {
            @Override
            protected ProviderType getRawValue(Provider object) {
                return object.getType();
            }
        };
        typeColumn.makeSortable(ProviderConditionFieldAutoCompleter.TYPE);

        getTable().addColumn(typeColumn, constants.typeProvider(), "200px"); //$NON-NLS-1$

        AbstractTextColumn<Provider> descriptionColumn = new AbstractTextColumn<Provider>() {
            @Override
            public String getValue(Provider object) {
                return object.getDescription();
            }
        };
        descriptionColumn.makeSortable(ProviderConditionFieldAutoCompleter.DESCRIPTION);

        getTable().addColumn(descriptionColumn, constants.descriptionProvider(), "300px"); //$NON-NLS-1$

        AbstractTextColumn<Provider> urlColumn = new AbstractTextColumn<Provider>() {
            @Override
            public String getValue(Provider object) {
                return object.getUrl();
            }
        };
        urlColumn.makeSortable(ProviderConditionFieldAutoCompleter.URL);

        getTable().addColumn(urlColumn, constants.urlProvider(), "300px"); //$NON-NLS-1$
    }

}
