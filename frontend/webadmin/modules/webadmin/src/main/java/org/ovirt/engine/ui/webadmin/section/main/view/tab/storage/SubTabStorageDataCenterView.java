package org.ovirt.engine.ui.webadmin.section.main.view.tab.storage;

import org.ovirt.engine.core.common.businessentities.StorageDomainStatus;
import org.ovirt.engine.core.common.businessentities.storage_domains;
import org.ovirt.engine.ui.uicommonweb.UICommand;
import org.ovirt.engine.ui.uicommonweb.models.storage.StorageDataCenterListModel;
import org.ovirt.engine.ui.uicommonweb.models.storage.StorageListModel;
import org.ovirt.engine.ui.webadmin.idhandler.ElementIdHandler;
import org.ovirt.engine.ui.webadmin.section.main.presenter.tab.storage.SubTabStorageDataCenterPresenter;
import org.ovirt.engine.ui.webadmin.section.main.view.AbstractSubTabTableView;
import org.ovirt.engine.ui.webadmin.uicommon.model.SearchableDetailModelProvider;
import org.ovirt.engine.ui.webadmin.widget.action.UiCommandButtonDefinition;
import org.ovirt.engine.ui.webadmin.widget.table.column.EnumColumn;
import org.ovirt.engine.ui.webadmin.widget.table.column.StorageDomainStatusColumn;
import org.ovirt.engine.ui.webadmin.widget.table.column.TextColumnWithTooltip;

import com.google.gwt.core.client.GWT;
import com.google.inject.Inject;

public class SubTabStorageDataCenterView extends AbstractSubTabTableView<storage_domains, storage_domains, StorageListModel, StorageDataCenterListModel>
        implements SubTabStorageDataCenterPresenter.ViewDef {

    interface ViewIdHandler extends ElementIdHandler<SubTabStorageDataCenterView> {
        ViewIdHandler idHandler = GWT.create(ViewIdHandler.class);
    }

    @Inject
    public SubTabStorageDataCenterView(SearchableDetailModelProvider<storage_domains, StorageListModel, StorageDataCenterListModel> modelProvider) {
        super(modelProvider);
        ViewIdHandler.idHandler.generateAndSetIds(this);
        initTable();
        initWidget(getTable());
    }

    void initTable() {
        getTable().addColumn(new StorageDomainStatusColumn(), "", "30px");

        TextColumnWithTooltip<storage_domains> nameColumn = new TextColumnWithTooltip<storage_domains>() {
            @Override
            public String getValue(storage_domains object) {
                return object.getstorage_pool_name();
            }
        };
        getTable().addColumn(nameColumn, "Name");

        TextColumnWithTooltip<storage_domains> domainStatusColumn =
                new EnumColumn<storage_domains, StorageDomainStatus>() {
                    @Override
                    protected StorageDomainStatus getRawValue(storage_domains object) {
                        return object.getstatus();
                    }
                };
        getTable().addColumn(domainStatusColumn, "Domain Status in Data-Center", "300px");

        getTable().addActionButton(new UiCommandButtonDefinition<storage_domains>("Attach") {
            @Override
            protected UICommand resolveCommand() {
                return getDetailModel().getAttachCommand();
            }
        });
        getTable().addActionButton(new UiCommandButtonDefinition<storage_domains>("Detach") {
            @Override
            protected UICommand resolveCommand() {
                return getDetailModel().getDetachCommand();
            }
        });
        getTable().addActionButton(new UiCommandButtonDefinition<storage_domains>("Activate") {
            @Override
            protected UICommand resolveCommand() {
                return getDetailModel().getActivateCommand();
            }
        });
        getTable().addActionButton(new UiCommandButtonDefinition<storage_domains>("Maintenance") {
            @Override
            protected UICommand resolveCommand() {
                return getDetailModel().getMaintenanceCommand();
            }
        });
    }

}
