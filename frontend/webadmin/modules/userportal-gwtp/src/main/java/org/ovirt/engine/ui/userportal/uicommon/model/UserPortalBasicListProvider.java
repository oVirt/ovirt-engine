package org.ovirt.engine.ui.userportal.uicommon.model;

import org.ovirt.engine.ui.uicommonweb.models.userportal.UserPortalBasicListModel;
import org.ovirt.engine.ui.uicommonweb.models.userportal.UserPortalItemModel;
import org.ovirt.engine.ui.userportal.gin.ClientGinjector;

import com.google.inject.Inject;

public class UserPortalBasicListProvider extends UserPortalDataBoundModelProvider<UserPortalItemModel, UserPortalBasicListModel> {

    @Inject
    public UserPortalBasicListProvider(ClientGinjector ginjector) {
        super(ginjector);
    }

    @Override
    protected UserPortalBasicListModel createModel() {
        return new UserPortalBasicListModel();
    }

}
