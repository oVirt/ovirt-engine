package org.ovirt.engine.ui.webadmin.section.main.view.tab.storage;

import org.ovirt.engine.core.common.businessentities.AuditLog;
import org.ovirt.engine.core.common.businessentities.storage_domains;
import org.ovirt.engine.ui.uicommonweb.models.storage.StorageEventListModel;
import org.ovirt.engine.ui.uicommonweb.models.storage.StorageListModel;
import org.ovirt.engine.ui.webadmin.section.main.presenter.tab.storage.SubTabStorageEventPresenter;
import org.ovirt.engine.ui.webadmin.section.main.view.AbstractSubTabEventView;
import org.ovirt.engine.ui.webadmin.uicommon.model.SearchableDetailModelProvider;

import com.google.inject.Inject;

public class SubTabStorageEventView extends AbstractSubTabEventView<storage_domains, StorageListModel, StorageEventListModel>
        implements SubTabStorageEventPresenter.ViewDef {

    @Inject
    public SubTabStorageEventView(SearchableDetailModelProvider<AuditLog, StorageListModel, StorageEventListModel> modelProvider) {
        super(modelProvider);
        initWidget(getTable());
    }

}
