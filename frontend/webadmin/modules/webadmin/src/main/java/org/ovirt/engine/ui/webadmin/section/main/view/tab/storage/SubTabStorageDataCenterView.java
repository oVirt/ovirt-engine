package org.ovirt.engine.ui.webadmin.section.main.view.tab.storage;

import org.ovirt.engine.core.common.businessentities.StorageDomainStatus;
import org.ovirt.engine.core.common.businessentities.storage_domains;
import org.ovirt.engine.ui.common.idhandler.ElementIdHandler;
import org.ovirt.engine.ui.common.uicommon.model.SearchableDetailModelProvider;
import org.ovirt.engine.ui.common.widget.table.column.EnumColumn;
import org.ovirt.engine.ui.common.widget.table.column.TextColumnWithTooltip;
import org.ovirt.engine.ui.uicommonweb.UICommand;
import org.ovirt.engine.ui.uicommonweb.models.storage.StorageDataCenterListModel;
import org.ovirt.engine.ui.uicommonweb.models.storage.StorageListModel;
import org.ovirt.engine.ui.webadmin.ApplicationConstants;
import org.ovirt.engine.ui.webadmin.section.main.presenter.tab.storage.SubTabStorageDataCenterPresenter;
import org.ovirt.engine.ui.webadmin.section.main.view.AbstractSubTabTableView;
import org.ovirt.engine.ui.webadmin.widget.action.WebAdminButtonDefinition;
import org.ovirt.engine.ui.webadmin.widget.table.column.StorageDomainStatusColumn;

import com.google.gwt.core.client.GWT;
import com.google.inject.Inject;

public class SubTabStorageDataCenterView extends AbstractSubTabTableView<storage_domains, storage_domains, StorageListModel, StorageDataCenterListModel>
        implements SubTabStorageDataCenterPresenter.ViewDef {

    interface ViewIdHandler extends ElementIdHandler<SubTabStorageDataCenterView> {
        ViewIdHandler idHandler = GWT.create(ViewIdHandler.class);
    }

    @Inject
    public SubTabStorageDataCenterView(SearchableDetailModelProvider<storage_domains, StorageListModel, StorageDataCenterListModel> modelProvider, ApplicationConstants constants) {
        super(modelProvider);
        ViewIdHandler.idHandler.generateAndSetIds(this);
        initTable(constants);
        initWidget(getTable());
    }

    void initTable(ApplicationConstants constants) {
        getTable().addColumn(new StorageDomainStatusColumn(), constants.empty(), "30px"); //$NON-NLS-1$

        TextColumnWithTooltip<storage_domains> nameColumn = new TextColumnWithTooltip<storage_domains>() {
            @Override
            public String getValue(storage_domains object) {
                return object.getstorage_pool_name();
            }
        };
        getTable().addColumn(nameColumn, constants.nameDc());

        TextColumnWithTooltip<storage_domains> domainStatusColumn =
                new EnumColumn<storage_domains, StorageDomainStatus>() {
                    @Override
                    protected StorageDomainStatus getRawValue(storage_domains object) {
                        return object.getstatus();
                    }
                };
        getTable().addColumn(domainStatusColumn, constants.domainStatusInDcStorageDc(), "300px"); //$NON-NLS-1$

        getTable().addActionButton(new WebAdminButtonDefinition<storage_domains>(constants.attachStorageDc()) {
            @Override
            protected UICommand resolveCommand() {
                return getDetailModel().getAttachCommand();
            }
        });
        getTable().addActionButton(new WebAdminButtonDefinition<storage_domains>(constants.detachStorageDc()) {
            @Override
            protected UICommand resolveCommand() {
                return getDetailModel().getDetachCommand();
            }
        });
        getTable().addActionButton(new WebAdminButtonDefinition<storage_domains>(constants.activateStorageDc()) {
            @Override
            protected UICommand resolveCommand() {
                return getDetailModel().getActivateCommand();
            }
        });
        getTable().addActionButton(new WebAdminButtonDefinition<storage_domains>(constants.maintenanceStorageDc()) {
            @Override
            protected UICommand resolveCommand() {
                return getDetailModel().getMaintenanceCommand();
            }
        });
    }

}
