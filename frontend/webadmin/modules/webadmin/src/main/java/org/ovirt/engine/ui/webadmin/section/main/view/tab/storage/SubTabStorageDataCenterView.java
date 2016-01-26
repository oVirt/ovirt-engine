package org.ovirt.engine.ui.webadmin.section.main.view.tab.storage;

import org.ovirt.engine.core.common.businessentities.StorageDomain;
import org.ovirt.engine.core.common.businessentities.StorageDomainStatus;
import org.ovirt.engine.ui.common.idhandler.ElementIdHandler;
import org.ovirt.engine.ui.common.uicommon.model.SearchableDetailModelProvider;
import org.ovirt.engine.ui.common.widget.table.column.AbstractEnumColumn;
import org.ovirt.engine.ui.common.widget.table.column.AbstractTextColumn;
import org.ovirt.engine.ui.uicommonweb.UICommand;
import org.ovirt.engine.ui.uicommonweb.models.storage.StorageDataCenterListModel;
import org.ovirt.engine.ui.uicommonweb.models.storage.StorageListModel;
import org.ovirt.engine.ui.webadmin.ApplicationConstants;
import org.ovirt.engine.ui.webadmin.gin.AssetProvider;
import org.ovirt.engine.ui.webadmin.section.main.presenter.tab.storage.SubTabStorageDataCenterPresenter;
import org.ovirt.engine.ui.webadmin.section.main.view.AbstractSubTabTableView;
import org.ovirt.engine.ui.webadmin.widget.action.WebAdminButtonDefinition;
import org.ovirt.engine.ui.webadmin.widget.table.column.StorageDomainStatusColumn;
import com.google.gwt.core.client.GWT;
import com.google.inject.Inject;

public class SubTabStorageDataCenterView extends AbstractSubTabTableView<StorageDomain, StorageDomain, StorageListModel, StorageDataCenterListModel>
        implements SubTabStorageDataCenterPresenter.ViewDef {

    interface ViewIdHandler extends ElementIdHandler<SubTabStorageDataCenterView> {
        ViewIdHandler idHandler = GWT.create(ViewIdHandler.class);
    }

    private static final ApplicationConstants constants = AssetProvider.getConstants();

    @Inject
    public SubTabStorageDataCenterView(SearchableDetailModelProvider<StorageDomain, StorageListModel, StorageDataCenterListModel> modelProvider) {
        super(modelProvider);
        initTable();
        initWidget(getTable());
    }

    @Override
    protected void generateIds() {
        ViewIdHandler.idHandler.generateAndSetIds(this);
    }

    void initTable() {
        getTable().enableColumnResizing();

        StorageDomainStatusColumn statusColumn = new StorageDomainStatusColumn();
        statusColumn.setContextMenuTitle(constants.statusStorageDc());
        getTable().addColumn(statusColumn, constants.empty(), "30px"); //$NON-NLS-1$

        AbstractTextColumn<StorageDomain> nameColumn = new AbstractTextColumn<StorageDomain>() {
            @Override
            public String getValue(StorageDomain object) {
                return object.getStoragePoolName();
            }
        };
        nameColumn.makeSortable();
        getTable().addColumn(nameColumn, constants.nameDc(), "600px"); //$NON-NLS-1$

        AbstractTextColumn<StorageDomain> domainStatusColumn =
                new AbstractEnumColumn<StorageDomain, StorageDomainStatus>() {
                    @Override
                    protected StorageDomainStatus getRawValue(StorageDomain object) {
                        return object.getStatus();
                    }
                };
        domainStatusColumn.makeSortable();
        getTable().addColumn(domainStatusColumn, constants.domainStatusInDcStorageDc(), "300px"); //$NON-NLS-1$

        getTable().addActionButton(new WebAdminButtonDefinition<StorageDomain>(constants.attachStorageDc()) {
            @Override
            protected UICommand resolveCommand() {
                return getDetailModel().getAttachCommand();
            }
        });
        getTable().addActionButton(new WebAdminButtonDefinition<StorageDomain>(constants.detachStorageDc()) {
            @Override
            protected UICommand resolveCommand() {
                return getDetailModel().getDetachCommand();
            }
        });
        getTable().addActionButton(new WebAdminButtonDefinition<StorageDomain>(constants.activateStorageDc()) {
            @Override
            protected UICommand resolveCommand() {
                return getDetailModel().getActivateCommand();
            }
        });
        getTable().addActionButton(new WebAdminButtonDefinition<StorageDomain>(constants.maintenanceStorageDc()) {
            @Override
            protected UICommand resolveCommand() {
                return getDetailModel().getMaintenanceCommand();
            }
        });
    }

}
