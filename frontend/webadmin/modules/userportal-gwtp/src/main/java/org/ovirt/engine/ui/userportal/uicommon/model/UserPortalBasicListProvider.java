package org.ovirt.engine.ui.userportal.uicommon.model;

import org.ovirt.engine.ui.uicommonweb.models.userportal.UserPortalBasicListModel;
import org.ovirt.engine.ui.uicommonweb.models.userportal.UserPortalItemModel;
import org.ovirt.engine.ui.userportal.gin.ClientGinjector;

import com.google.inject.Inject;

public class UserPortalBasicListProvider extends UserPortalDataBoundModelProvider<UserPortalItemModel, UserPortalBasicListModel> {

    private final UserPortalItemModelKeyProvider keyProvider;

    @Inject
    public UserPortalBasicListProvider(ClientGinjector ginjector, UserPortalItemModelKeyProvider keyProvider) {
        super(ginjector);
        this.keyProvider = keyProvider;
    }

    @Override
    protected UserPortalBasicListModel createModel() {
        return new UserPortalBasicListModel();
    }

    @Override
    public Object getKey(UserPortalItemModel item) {
        return keyProvider.getKey(item);
    }

}
