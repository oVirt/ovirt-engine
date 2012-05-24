package org.ovirt.engine.ui.uicommonweb.models.vms;

import org.ovirt.engine.ui.uicommonweb.UICommand;
import org.ovirt.engine.ui.uicommonweb.models.EntityModel;

public class VncInfoModel extends EntityModel {
    EntityModel vncMessage;
    UICommand closeCommand;

    public EntityModel getVncMessage() {
        return vncMessage;
    }

    public void setVncMessage(EntityModel vncMessage) {
        this.vncMessage = vncMessage;
    }

    public UICommand getCloseCommand() {
        return closeCommand;
    }

    public void setCloseCommand(UICommand closeCommand) {
        this.closeCommand = closeCommand;
    }

    public VncInfoModel() {
        setVncMessage(new EntityModel());
    }
}
