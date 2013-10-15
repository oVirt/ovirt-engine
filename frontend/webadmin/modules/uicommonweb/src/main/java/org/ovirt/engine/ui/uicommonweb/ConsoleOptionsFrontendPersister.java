package org.ovirt.engine.ui.uicommonweb;

import org.ovirt.engine.ui.uicommonweb.models.VmConsoles;

public interface ConsoleOptionsFrontendPersister {

    void storeToLocalStorage(VmConsoles vmConsoles);

    void loadFromLocalStorage(VmConsoles vmConsoles);

    /**
     * The stored/loaded entities can not interfere from one app/view to other even it is all in one browser.
     */
    public static enum ConsoleContext {
        UP_BASIC,
        UP_EXTENDED,
        WA;
    }
}
