package org.ovirt.engine.ui.uicommonweb;

import org.ovirt.engine.ui.uicommonweb.models.HasConsoleModel;

public interface ConsoleOptionsFrontendPersister {

    void storeToLocalStorage(HasConsoleModel model, ConsoleContext context);

    void loadFromLocalStorage(HasConsoleModel model, ConsoleContext context);

    /**
     * The stored/loaded entities can not interfere from one app/view to other even it is all in one browser.
     */
    public static enum ConsoleContext {
        UP_BASIC,
        UP_EXTENDED,
        WA;
    }
}
