package org.ovirt.engine.ui.uicommonweb.models;

import org.ovirt.engine.ui.uicommonweb.ConsoleOptionsFrontendPersister.ConsoleContext;
import org.ovirt.engine.ui.uicompat.ConstantsManager;

public class ConsolePopupModel extends Model {

    private HasConsoleModel consoleModel;

    private ConsoleContext consoleContext;

    public ConsolePopupModel() {
        setTitle(ConstantsManager.getInstance().getConstants().consoleOptions());
    }

    public HasConsoleModel getModel() {
        return consoleModel;
    }

    public void setModel(HasConsoleModel model) {
        this.consoleModel = model;
    }

    public ConsoleContext getConsoleContext() {
        return consoleContext;
    }

    public void setConsoleContext(ConsoleContext consoleContext) {
        this.consoleContext = consoleContext;
    }
}
