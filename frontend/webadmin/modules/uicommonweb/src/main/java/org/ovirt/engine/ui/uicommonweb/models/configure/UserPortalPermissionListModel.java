package org.ovirt.engine.ui.uicommonweb.models.configure;

import org.ovirt.engine.ui.uicommonweb.models.users.AdElementListModel;
import org.ovirt.engine.ui.uicommonweb.models.users.UserPortalAdElementListModel;

public class UserPortalPermissionListModel extends PermissionListModel {
    @Override
    protected AdElementListModel createAdElementListModel() {
        return new UserPortalAdElementListModel();
    }
}
