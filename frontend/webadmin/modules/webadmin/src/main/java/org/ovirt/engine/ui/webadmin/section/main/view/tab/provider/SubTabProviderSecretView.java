package org.ovirt.engine.ui.webadmin.section.main.view.tab.provider;

import java.util.Date;

import javax.inject.Inject;

import org.ovirt.engine.core.common.businessentities.Provider;
import org.ovirt.engine.core.common.businessentities.storage.LibvirtSecret;
import org.ovirt.engine.core.common.businessentities.storage.LibvirtSecretUsageType;
import org.ovirt.engine.ui.common.idhandler.ElementIdHandler;
import org.ovirt.engine.ui.common.uicommon.model.SearchableDetailModelProvider;
import org.ovirt.engine.ui.common.widget.table.column.AbstractEnumColumn;
import org.ovirt.engine.ui.common.widget.table.column.AbstractFullDateTimeColumn;
import org.ovirt.engine.ui.common.widget.table.column.AbstractTextColumn;
import org.ovirt.engine.ui.uicommonweb.models.providers.ProviderListModel;
import org.ovirt.engine.ui.uicommonweb.models.providers.ProviderSecretListModel;
import org.ovirt.engine.ui.webadmin.ApplicationConstants;
import org.ovirt.engine.ui.webadmin.gin.AssetProvider;
import org.ovirt.engine.ui.webadmin.section.main.presenter.tab.provider.SubTabProviderSecretPresenter;
import org.ovirt.engine.ui.webadmin.section.main.view.AbstractSubTabTableView;

import com.google.gwt.core.client.GWT;

public class SubTabProviderSecretView extends AbstractSubTabTableView<Provider, LibvirtSecret, ProviderListModel, ProviderSecretListModel>
        implements SubTabProviderSecretPresenter.ViewDef {

    interface ViewIdHandler extends ElementIdHandler<SubTabProviderSecretView> {
        ViewIdHandler idHandler = GWT.create(ViewIdHandler.class);
    }

    private static final ApplicationConstants constants = AssetProvider.getConstants();

    @Inject
    public SubTabProviderSecretView(SearchableDetailModelProvider<LibvirtSecret, ProviderListModel, ProviderSecretListModel> modelProvider) {
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

        AbstractTextColumn<LibvirtSecret> idColumn = new AbstractTextColumn<LibvirtSecret>() {
            @Override
            public String getValue(LibvirtSecret object) {
                return object.getId().toString();
            }
        };
        idColumn.makeSortable();
        getTable().addColumn(idColumn, constants.idLibvirtSecret(), "300px"); //$NON-NLS-1$

        AbstractTextColumn<LibvirtSecret> usageTypeColumn =
                new AbstractEnumColumn<LibvirtSecret, LibvirtSecretUsageType>() {
                    @Override
                    public LibvirtSecretUsageType getRawValue(LibvirtSecret object) {
                        return object.getUsageType();
                    }
                };
        usageTypeColumn.makeSortable();
        getTable().addColumn(usageTypeColumn, constants.usageTypeLibvirtSecret(), "150px"); //$NON-NLS-1$

        AbstractTextColumn<LibvirtSecret> descriptionColumn = new AbstractTextColumn<LibvirtSecret>() {
            @Override
            public String getValue(LibvirtSecret object) {
                return object.getDescription();
            }
        };
        descriptionColumn.makeSortable();
        getTable().addColumn(descriptionColumn, constants.descriptionLibvirtSecret(), "200px"); //$NON-NLS-1$


        AbstractFullDateTimeColumn<LibvirtSecret> creationDateColumn = new AbstractFullDateTimeColumn<LibvirtSecret>() {
            @Override
            public Date getRawValue(LibvirtSecret object) {
                return object.getCreationDate();
            }
        };
        creationDateColumn.makeSortable();
        getTable().addColumn(creationDateColumn, constants.creationDateLibvirtSecret(), "200px"); //$NON-NLS-1$
    }
}
