package org.ovirt.engine.ui.userportal.uicommon.model;

import org.ovirt.engine.ui.uicommonweb.models.userportal.UserPortalItemModel;
import org.ovirt.engine.ui.uicommonweb.models.userportal.UserPortalListModel;
import org.ovirt.engine.ui.userportal.gin.ClientGinjector;

import com.google.inject.Inject;

public class UserPortalListProvider extends UserPortalDataBoundModelProvider<UserPortalItemModel, UserPortalListModel> {

    private final UserPortalItemModelKeyProvider keyProvider;

    @Inject
    public UserPortalListProvider(ClientGinjector ginjector, UserPortalItemModelKeyProvider keyProvider) {
        super(ginjector);
        this.keyProvider = keyProvider;
    }

    @Override
    protected UserPortalListModel createModel() {
        return new UserPortalListModel();
    }

}
