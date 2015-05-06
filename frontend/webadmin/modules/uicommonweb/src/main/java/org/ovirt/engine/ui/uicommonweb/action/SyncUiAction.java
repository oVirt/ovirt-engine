package org.ovirt.engine.ui.uicommonweb.action;

import org.ovirt.engine.ui.uicommonweb.models.Model;

public abstract class SyncUiAction extends UiAction {

    public SyncUiAction(Model model, String name) {
        super(model, name);
    }

    void internalRunAction() {
        onActionExecute();
        runNextAction();
    }

    /**
     * This method contains the code that will run when the action is executed.
     */
    protected abstract void onActionExecute();
}
