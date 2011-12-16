package org.ovirt.engine.ui.webadmin.section.main.view.tab.host;

import javax.inject.Inject;

import org.ovirt.engine.core.common.businessentities.VDS;
import org.ovirt.engine.core.common.businessentities.permissions;
import org.ovirt.engine.ui.uicommonweb.models.configure.PermissionListModel;
import org.ovirt.engine.ui.uicommonweb.models.hosts.HostListModel;
import org.ovirt.engine.ui.webadmin.section.main.presenter.tab.host.SubTabHostPermissionPresenter;
import org.ovirt.engine.ui.webadmin.section.main.view.tab.AbstractSubTabPermissionsView;
import org.ovirt.engine.ui.webadmin.uicommon.model.SearchableDetailModelProvider;

public class SubTabHostPermissionView extends AbstractSubTabPermissionsView<VDS, HostListModel>
        implements SubTabHostPermissionPresenter.ViewDef {

    @Inject
    public SubTabHostPermissionView(SearchableDetailModelProvider<permissions, HostListModel, PermissionListModel> modelProvider) {
        super(modelProvider);
        initWidget(getTable());
    }

}
