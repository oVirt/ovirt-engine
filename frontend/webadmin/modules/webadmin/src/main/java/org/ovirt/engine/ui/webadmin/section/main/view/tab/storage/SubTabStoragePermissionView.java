package org.ovirt.engine.ui.webadmin.section.main.view.tab.storage;

import org.ovirt.engine.core.common.businessentities.permissions;
import org.ovirt.engine.core.common.businessentities.storage_domains;
import org.ovirt.engine.ui.uicommonweb.models.configure.PermissionListModel;
import org.ovirt.engine.ui.uicommonweb.models.storage.StorageListModel;
import org.ovirt.engine.ui.webadmin.section.main.presenter.tab.storage.SubTabStoragePermissionPresenter;
import org.ovirt.engine.ui.webadmin.section.main.view.tab.AbstrctSubTabPermissionsView;
import org.ovirt.engine.ui.webadmin.uicommon.model.SearchableDetailModelProvider;

import com.google.inject.Inject;

public class SubTabStoragePermissionView extends AbstrctSubTabPermissionsView<storage_domains, StorageListModel>
        implements SubTabStoragePermissionPresenter.ViewDef {

    @Inject
    public SubTabStoragePermissionView(SearchableDetailModelProvider<permissions, StorageListModel, PermissionListModel> modelProvider) {
        super(modelProvider);
        initWidget(getTable());
    }

}
