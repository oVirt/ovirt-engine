package org.ovirt.engine.ui.uicommonweb.models.userportal;

import org.ovirt.engine.ui.uicommonweb.models.Model;
import org.ovirt.engine.ui.uicompat.ConstantsManager;

public class UserPortalConsolePopupModel extends Model {

    private IUserPortalListModel model;

    public UserPortalConsolePopupModel() {
        setTitle(ConstantsManager.getInstance().getConstants().consoleOptions());
    }

    public IUserPortalListModel getModel() {
        return model;
    }

    public void setModel(IUserPortalListModel model) {
        this.model = model;
    }

}
