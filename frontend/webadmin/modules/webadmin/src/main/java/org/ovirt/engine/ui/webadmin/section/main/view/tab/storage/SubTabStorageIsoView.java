package org.ovirt.engine.ui.webadmin.section.main.view.tab.storage;

import org.ovirt.engine.core.common.businessentities.storage_domains;
import org.ovirt.engine.ui.uicommonweb.models.EntityModel;
import org.ovirt.engine.ui.uicommonweb.models.storage.StorageIsoListModel;
import org.ovirt.engine.ui.uicommonweb.models.storage.StorageListModel;
import org.ovirt.engine.ui.webadmin.section.main.presenter.tab.storage.SubTabStorageIsoPresenter;
import org.ovirt.engine.ui.webadmin.section.main.view.AbstractSubTabTableView;
import org.ovirt.engine.ui.webadmin.uicommon.model.SearchableDetailModelProvider;

import com.google.gwt.user.cellview.client.TextColumn;
import com.google.inject.Inject;

public class SubTabStorageIsoView extends AbstractSubTabTableView<storage_domains, EntityModel, StorageListModel, StorageIsoListModel>
        implements SubTabStorageIsoPresenter.ViewDef {

    @Inject
    public SubTabStorageIsoView(SearchableDetailModelProvider<EntityModel, StorageListModel, StorageIsoListModel> modelProvider) {
        super(modelProvider);
        initTable();
        initWidget(getTable());
    }

    void initTable() {
        TextColumn<EntityModel> fileNameColumn = new TextColumn<EntityModel>() {
            @Override
            public String getValue(EntityModel object) {
                return object.getTitle();
            }
        };
        getTable().addColumn(fileNameColumn, "File Name");

        TextColumn<EntityModel> typeColumn = new TextColumn<EntityModel>() {
            @Override
            public String getValue(EntityModel object) {
                return object.getEntity().toString();
            }
        };
        getTable().addColumn(typeColumn, "Type");
    }

}
