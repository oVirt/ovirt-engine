package org.ovirt.engine.ui.uicommonweb.models;

import org.ovirt.engine.ui.uicompat.ConstantsManager;

public class ConsolePopupModel extends Model {

    private HasConsoleModel consoleModel;

    public ConsolePopupModel() {
        setTitle(ConstantsManager.getInstance().getConstants().consoleOptions());
    }

    public HasConsoleModel getModel() {
        return consoleModel;
    }

    public void setModel(HasConsoleModel model) {
        this.consoleModel = model;
    }

}
