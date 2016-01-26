package org.ovirt.engine.ui.webadmin.section.main.view.tab;

import org.ovirt.engine.core.common.businessentities.Provider;
import org.ovirt.engine.core.common.businessentities.ProviderType;
import org.ovirt.engine.core.searchbackend.ProviderConditionFieldAutoCompleter;
import org.ovirt.engine.ui.common.idhandler.ElementIdHandler;
import org.ovirt.engine.ui.common.uicommon.model.MainModelProvider;
import org.ovirt.engine.ui.common.widget.table.column.AbstractEnumColumn;
import org.ovirt.engine.ui.common.widget.table.column.AbstractTextColumn;
import org.ovirt.engine.ui.uicommonweb.UICommand;
import org.ovirt.engine.ui.uicommonweb.models.providers.ProviderListModel;
import org.ovirt.engine.ui.webadmin.ApplicationConstants;
import org.ovirt.engine.ui.webadmin.gin.AssetProvider;
import org.ovirt.engine.ui.webadmin.section.main.presenter.tab.MainTabProviderPresenter;
import org.ovirt.engine.ui.webadmin.section.main.view.AbstractMainTabWithDetailsTableView;
import org.ovirt.engine.ui.webadmin.widget.action.WebAdminButtonDefinition;
import com.google.gwt.core.client.GWT;
import com.google.inject.Inject;

public class MainTabProviderView extends AbstractMainTabWithDetailsTableView<Provider, ProviderListModel> implements MainTabProviderPresenter.ViewDef {

    interface ViewIdHandler extends ElementIdHandler<MainTabProviderView> {
        ViewIdHandler idHandler = GWT.create(ViewIdHandler.class);
    }

    private static final ApplicationConstants constants = AssetProvider.getConstants();

    @Inject
    public MainTabProviderView(MainModelProvider<Provider, ProviderListModel> modelProvider) {
        super(modelProvider);
        ViewIdHandler.idHandler.generateAndSetIds(this);
        initTable();
        initWidget(getTable());
    }

    void initTable() {
        getTable().enableColumnResizing();

        AbstractTextColumn<Provider> nameColumn = new AbstractTextColumn<Provider>() {
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

        getTable().addActionButton(new WebAdminButtonDefinition<Provider>(constants.addProvider()) {
            @Override
            protected UICommand resolveCommand() {
                return getMainModel().getAddCommand();
            }
        });

        getTable().addActionButton(new WebAdminButtonDefinition<Provider>(constants.editProvider()) {
            @Override
            protected UICommand resolveCommand() {
                return getMainModel().getEditCommand();
            }
        });

        getTable().addActionButton(new WebAdminButtonDefinition<Provider>(constants.removeProvider()) {
            @Override
            protected UICommand resolveCommand() {
                return getMainModel().getRemoveCommand();
            }
        });
    }

}
