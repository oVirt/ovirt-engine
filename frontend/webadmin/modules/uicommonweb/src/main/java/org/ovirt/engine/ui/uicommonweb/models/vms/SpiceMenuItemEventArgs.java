package org.ovirt.engine.ui.uicommonweb.models.vms;

import org.ovirt.engine.ui.uicompat.EventArgs;

@SuppressWarnings("unused")
public final class SpiceMenuItemEventArgs extends EventArgs {
    private int privateMenuItemId;

    public int getMenuItemId() {
        return privateMenuItemId;
    }

    private void setMenuItemId(int value) {
        privateMenuItemId = value;
    }

    public SpiceMenuItemEventArgs(int menuItemId) {
        setMenuItemId(menuItemId);
    }
}
