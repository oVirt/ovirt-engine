package org.ovirt.engine.ui.webadmin.section.main.view.tab;

import java.util.List;

import org.ovirt.engine.core.common.businessentities.StorageDomainSharedStatus;
import org.ovirt.engine.core.common.businessentities.StorageDomainType;
import org.ovirt.engine.core.common.businessentities.StorageFormatType;
import org.ovirt.engine.core.common.businessentities.StorageType;
import org.ovirt.engine.core.common.businessentities.storage_domains;
import org.ovirt.engine.ui.common.idhandler.ElementIdHandler;
import org.ovirt.engine.ui.common.uicommon.model.MainModelProvider;
import org.ovirt.engine.ui.common.widget.action.ActionButtonDefinition;
import org.ovirt.engine.ui.common.widget.action.CommandLocation;
import org.ovirt.engine.ui.common.widget.renderer.DiskSizeRenderer.DiskSizeUnit;
import org.ovirt.engine.ui.common.widget.table.column.DiskSizeColumn;
import org.ovirt.engine.ui.common.widget.table.column.EnumColumn;
import org.ovirt.engine.ui.common.widget.table.column.TextColumnWithTooltip;
import org.ovirt.engine.ui.uicommonweb.ReportInit;
import org.ovirt.engine.ui.uicommonweb.UICommand;
import org.ovirt.engine.ui.uicommonweb.models.storage.StorageListModel;
import org.ovirt.engine.ui.webadmin.section.main.presenter.tab.MainTabStoragePresenter;
import org.ovirt.engine.ui.webadmin.section.main.view.AbstractMainTabWithDetailsTableView;
import org.ovirt.engine.ui.webadmin.uicommon.ReportActionsHelper;
import org.ovirt.engine.ui.webadmin.widget.action.WebAdminButtonDefinition;
import org.ovirt.engine.ui.webadmin.widget.action.WebAdminMenuBarButtonDefinition;
import org.ovirt.engine.ui.webadmin.widget.table.column.StorageDomainSharedStatusColumn;

import com.google.gwt.core.client.GWT;
import com.google.inject.Inject;

public class MainTabStorageView extends AbstractMainTabWithDetailsTableView<storage_domains, StorageListModel> implements MainTabStoragePresenter.ViewDef {

    interface ViewIdHandler extends ElementIdHandler<MainTabStorageView> {
        ViewIdHandler idHandler = GWT.create(ViewIdHandler.class);
    }

    @Inject
    public MainTabStorageView(MainModelProvider<storage_domains, StorageListModel> modelProvider) {
        super(modelProvider);
        ViewIdHandler.idHandler.generateAndSetIds(this);
        initTable();
        initWidget(getTable());
    }

    void initTable() {
        getTable().addColumn(new StorageDomainSharedStatusColumn(), "", "30px");

        TextColumnWithTooltip<storage_domains> nameColumn = new TextColumnWithTooltip<storage_domains>() {
            @Override
            public String getValue(storage_domains object) {
                return object.getstorage_name();
            }
        };
        getTable().addColumn(nameColumn, "Domain Name");

        TextColumnWithTooltip<storage_domains> domainTypeColumn = new EnumColumn<storage_domains, StorageDomainType>() {
            @Override
            protected StorageDomainType getRawValue(storage_domains object) {
                return object.getstorage_domain_type();
            }
        };
        getTable().addColumn(domainTypeColumn, "Domain Type");

        TextColumnWithTooltip<storage_domains> storageTypeColumn = new EnumColumn<storage_domains, StorageType>() {
            @Override
            protected StorageType getRawValue(storage_domains object) {
                return object.getstorage_type();
            }
        };
        getTable().addColumn(storageTypeColumn, "Storage Type");

        TextColumnWithTooltip<storage_domains> formatColumn = new EnumColumn<storage_domains, StorageFormatType>() {
            @Override
            protected StorageFormatType getRawValue(storage_domains object) {
                return object.getStorageFormat();
            }
        };
        getTable().addColumn(formatColumn, "Format");

        TextColumnWithTooltip<storage_domains> crossDataCenterStatusColumn =
                new EnumColumn<storage_domains, StorageDomainSharedStatus>() {
                    @Override
                    protected StorageDomainSharedStatus getRawValue(storage_domains object) {
                        return object.getstorage_domain_shared_status();
                    }
                };
        getTable().addColumn(crossDataCenterStatusColumn, "Cross Data-Center Status");

        DiskSizeColumn<storage_domains> freeSpaceColumn = new DiskSizeColumn<storage_domains>(DiskSizeUnit.GIGABYTE) {
            @Override
            public Long getRawValue(storage_domains object) {
                long availableDiskSize = object.getavailable_disk_size() != null ? object.getavailable_disk_size() : 0;
                return (long) availableDiskSize;
            }
        };
        getTable().addColumn(freeSpaceColumn, "Free Space");

        getTable().addActionButton(new WebAdminButtonDefinition<storage_domains>("New Domain") {
            @Override
            protected UICommand resolveCommand() {
                return getMainModel().getNewDomainCommand();
            }
        });
        getTable().addActionButton(new WebAdminButtonDefinition<storage_domains>("Import Domain") {
            @Override
            protected UICommand resolveCommand() {
                return getMainModel().getImportDomainCommand();
            }
        });
        getTable().addActionButton(new WebAdminButtonDefinition<storage_domains>("Edit") {
            @Override
            protected UICommand resolveCommand() {
                return getMainModel().getEditCommand();
            }
        });
        getTable().addActionButton(new WebAdminButtonDefinition<storage_domains>("Remove") {
            @Override
            protected UICommand resolveCommand() {
                return getMainModel().getRemoveCommand();
            }
        });
        getTable().addActionButton(new WebAdminButtonDefinition<storage_domains>("Destroy", CommandLocation.OnlyFromFromContext) {
            @Override
            protected UICommand resolveCommand() {
                return getMainModel().getDestroyCommand();
            }
        });
        if (ReportInit.getInstance().isReportsEnabled()) {
            List<ActionButtonDefinition<storage_domains>> resourceSubActions =
                    ReportActionsHelper.getInstance().getResourceSubActions("Storage", getMainModel());
            if (resourceSubActions != null && resourceSubActions.size() > 0) {
                getTable().addActionButton(new WebAdminMenuBarButtonDefinition<storage_domains>("Show Report",
                        resourceSubActions));
            }
        }
    }

}
