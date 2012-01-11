package org.ovirt.engine.ui.webadmin.section.main.view.tab.storage;

import org.ovirt.engine.core.common.businessentities.storage_domains;
import org.ovirt.engine.ui.common.uicommon.model.SearchableDetailModelProvider;
import org.ovirt.engine.ui.common.widget.table.column.TextColumnWithTooltip;
import org.ovirt.engine.ui.uicommonweb.models.EntityModel;
import org.ovirt.engine.ui.uicommonweb.models.storage.StorageIsoListModel;
import org.ovirt.engine.ui.uicommonweb.models.storage.StorageListModel;
import org.ovirt.engine.ui.webadmin.ApplicationResources;
import org.ovirt.engine.ui.webadmin.section.main.presenter.tab.storage.SubTabStorageIsoPresenter;
import org.ovirt.engine.ui.webadmin.section.main.view.AbstractSubTabTableView;

import com.google.inject.Inject;

public class SubTabStorageIsoView extends AbstractSubTabTableView<storage_domains, EntityModel, StorageListModel, StorageIsoListModel>
        implements SubTabStorageIsoPresenter.ViewDef {

    @Inject
    public SubTabStorageIsoView(SearchableDetailModelProvider<EntityModel, StorageListModel, StorageIsoListModel> modelProvider,
            ApplicationResources resources) {
        super(modelProvider);
        initTable(resources);
        initWidget(getTable());
    }

    void initTable(ApplicationResources resources) {
        TextColumnWithTooltip<EntityModel> fileNameColumn = new TextColumnWithTooltip<EntityModel>() {
            @Override
            public String getValue(EntityModel object) {
                return object.getTitle();
            }
        };
        getTable().addColumn(fileNameColumn, "File Name");

        TextColumnWithTooltip<EntityModel> typeColumn = new TextColumnWithTooltip<EntityModel>() {
            @Override
            public String getValue(EntityModel object) {
                return object.getEntity().toString();
            }
        };
        getTable().addColumn(typeColumn, "Type");

        getTable().showRefreshButton();
    }
}
