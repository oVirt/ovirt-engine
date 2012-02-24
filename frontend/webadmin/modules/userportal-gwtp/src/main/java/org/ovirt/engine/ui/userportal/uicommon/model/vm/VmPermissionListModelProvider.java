package org.ovirt.engine.ui.userportal.uicommon.model.vm;

import org.ovirt.engine.core.common.businessentities.permissions;
import org.ovirt.engine.ui.uicommonweb.models.configure.PermissionListModel;
import org.ovirt.engine.ui.uicommonweb.models.userportal.UserPortalListModel;
import org.ovirt.engine.ui.userportal.gin.ClientGinjector;
import org.ovirt.engine.ui.userportal.uicommon.model.UserPortalListProvider;
import org.ovirt.engine.ui.userportal.uicommon.model.UserPortalModelResolver;
import org.ovirt.engine.ui.userportal.uicommon.model.UserPortalSearchableDetailModelProvider;

import com.google.inject.Inject;

public class VmPermissionListModelProvider
        extends UserPortalSearchableDetailModelProvider<permissions, UserPortalListModel, PermissionListModel> {

    @Inject
    public VmPermissionListModelProvider(ClientGinjector ginjector,
            UserPortalListProvider parentProvider,
            UserPortalModelResolver resolver) {
        super(ginjector, parentProvider, PermissionListModel.class, resolver);
    }

    @Override
    protected PermissionListModel createModel() {
        return new PermissionListModel();
    }

}
