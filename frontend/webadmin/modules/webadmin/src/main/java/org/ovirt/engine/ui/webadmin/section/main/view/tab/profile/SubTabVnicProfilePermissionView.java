package org.ovirt.engine.ui.webadmin.section.main.view.tab.profile;

import org.ovirt.engine.core.common.businessentities.Permission;
import org.ovirt.engine.core.common.businessentities.network.VnicProfileView;
import org.ovirt.engine.ui.common.idhandler.ElementIdHandler;
import org.ovirt.engine.ui.common.system.ClientStorage;
import org.ovirt.engine.ui.common.uicommon.model.SearchableDetailModelProvider;
import org.ovirt.engine.ui.uicommonweb.models.configure.PermissionListModel;
import org.ovirt.engine.ui.uicommonweb.models.profiles.VnicProfileListModel;
import org.ovirt.engine.ui.webadmin.section.main.presenter.tab.profile.SubTabVnicProfilePermissionPresenter;
import org.ovirt.engine.ui.webadmin.section.main.view.tab.AbstractSubTabPermissionsView;
import com.google.gwt.core.client.GWT;
import com.google.gwt.event.shared.EventBus;
import com.google.inject.Inject;

public class SubTabVnicProfilePermissionView extends AbstractSubTabPermissionsView<VnicProfileView, VnicProfileListModel>
        implements SubTabVnicProfilePermissionPresenter.ViewDef {

    interface ViewIdHandler extends ElementIdHandler<SubTabVnicProfilePermissionView> {
        ViewIdHandler idHandler = GWT.create(ViewIdHandler.class);
    }

    @Inject
    public SubTabVnicProfilePermissionView(SearchableDetailModelProvider<Permission, VnicProfileListModel,
            PermissionListModel<VnicProfileView>> modelProvider, EventBus eventBus,
            ClientStorage clientStorage) {
        super(modelProvider, eventBus, clientStorage);
        getTable().enableColumnResizing();
    }

    @Override
    protected void generateIds() {
        ViewIdHandler.idHandler.generateAndSetIds(this);
    }

}
