package org.ovirt.engine.ui.uicommonweb.models.vms;

@SuppressWarnings("unused")
public abstract class SpiceMenuItem {
    private int privateId;

    public int getId() {
        return privateId;
    }

    public void setId(int value) {
        privateId = value;
    }

    private boolean privateIsEnabled;

    public boolean getIsEnabled() {
        return privateIsEnabled;
    }

    public void setIsEnabled(boolean value) {
        privateIsEnabled = value;
    }

    protected SpiceMenuItem() {
        setIsEnabled(true);
    }
}
