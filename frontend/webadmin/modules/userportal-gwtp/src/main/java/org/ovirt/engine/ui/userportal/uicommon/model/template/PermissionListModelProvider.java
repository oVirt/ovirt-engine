package org.ovirt.engine.ui.userportal.uicommon.model.template;

import org.ovirt.engine.core.common.businessentities.permissions;
import org.ovirt.engine.ui.uicommonweb.models.configure.PermissionListModel;
import org.ovirt.engine.ui.uicommonweb.models.userportal.UserPortalTemplateListModel;
import org.ovirt.engine.ui.userportal.gin.ClientGinjector;
import org.ovirt.engine.ui.userportal.uicommon.model.UserPortalModelResolver;
import org.ovirt.engine.ui.userportal.uicommon.model.UserPortalSearchableDetailModelProvider;

import com.google.inject.Inject;

public class PermissionListModelProvider
        extends UserPortalSearchableDetailModelProvider<permissions, UserPortalTemplateListModel, PermissionListModel> {

    @Inject
    public PermissionListModelProvider(ClientGinjector ginjector,
            UserPortalTemplateListProvider parentProvider,
            UserPortalModelResolver resolver) {
        super(ginjector, parentProvider, PermissionListModel.class, resolver);
    }

    @Override
    protected PermissionListModel createModel() {
        return new PermissionListModel();
    }
}
