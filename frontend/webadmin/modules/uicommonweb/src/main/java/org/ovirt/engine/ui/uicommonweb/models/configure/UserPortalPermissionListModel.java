package org.ovirt.engine.ui.uicommonweb.models.configure;

import org.ovirt.engine.ui.uicommonweb.models.users.AdElementListModel;

import com.google.inject.Inject;
import com.google.inject.Provider;

public class UserPortalPermissionListModel<E> extends PermissionListModel<E> {

    @Inject
    public UserPortalPermissionListModel(Provider<AdElementListModel> adElementListModelProvider) {
        super(adElementListModelProvider);
    }

    @Override
    public boolean getAllUsersWithPermission() {
        return true;
    }
}
