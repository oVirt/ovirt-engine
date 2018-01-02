package org.ovirt.engine.ui.uicommonweb.models.gluster;

import org.ovirt.engine.ui.uicommonweb.models.ConfirmationModel;

public class ResetBrickModel extends ConfirmationModel{

    private String validationMessage;

    public String getValidationMessage() {
        return validationMessage;
    }

    public void setValidationMessage(String validationMessage) {
        this.validationMessage = validationMessage;
    }
}
