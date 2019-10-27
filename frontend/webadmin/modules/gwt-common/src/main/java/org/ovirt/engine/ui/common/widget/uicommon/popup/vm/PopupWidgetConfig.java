package org.ovirt.engine.ui.common.widget.uicommon.popup.vm;

public class PopupWidgetConfig {

    private final boolean advancedOnly;

    private final boolean alwaysHidden;

    private final boolean detachable;

    private final boolean adminOnly;

    private final boolean managedOnly;

    /**
     * The only mutable part - the field can be visible/hidden according to some changing condition in the app
     */
    private boolean applicationLevelVisible = true;

    private PopupWidgetConfig(boolean advancedOnly,
                              boolean alwaysHidden,
                              boolean detachable,
                              boolean adminOnly,
                              boolean managedOnly) {
        super();
        this.advancedOnly = advancedOnly;
        this.alwaysHidden = alwaysHidden;
        this.detachable = detachable;
        this.adminOnly = adminOnly;
        this.managedOnly = managedOnly;
    }

    public static PopupWidgetConfig simpleField() {
        return new PopupWidgetConfig(false, false, false, false, false);
    }

    public static PopupWidgetConfig hiddenField() {
        return new PopupWidgetConfig(false, true, false, false, false);
    }

    public PopupWidgetConfig detachable() {
        return new PopupWidgetConfig(advancedOnly, alwaysHidden, true, adminOnly, managedOnly);
    }

    public PopupWidgetConfig visibleInAdvancedModeOnly() {
        return new PopupWidgetConfig(true, alwaysHidden, detachable, adminOnly, managedOnly);
    }

    public PopupWidgetConfig visibleForAdminOnly() {
        return new PopupWidgetConfig(advancedOnly, alwaysHidden, detachable, true, managedOnly);
    }

    public PopupWidgetConfig visibleForManagedOnly() {
        return new PopupWidgetConfig(advancedOnly, alwaysHidden, detachable, adminOnly, true);
    }

    public PopupWidgetConfig copy() {
        PopupWidgetConfig copy = new PopupWidgetConfig(advancedOnly, alwaysHidden, detachable, adminOnly, managedOnly);
        copy.setApplicationLevelVisible(isApplicationLevelVisible());
        return copy;
    }

    public boolean isVisibleOnlyInAdvanced() {
        return advancedOnly;
    }

    public boolean isAlwaysHidden() {
        return alwaysHidden;
    }

    public boolean isDetachable() {
        return detachable;
    }

    public boolean isAdminOnly() {
        return adminOnly;
    }

    public boolean isApplicationLevelVisible() {
        return applicationLevelVisible;
    }

    public boolean isManagedOnly() {
        return managedOnly;
    }

    public void setApplicationLevelVisible(boolean applicationLevelVisible) {
        this.applicationLevelVisible = applicationLevelVisible;
    }

    public boolean isCurrentlyVisible(boolean advancedMode, boolean createInstanceMode, boolean managed) {
        if (isAlwaysHidden()) {
            return false;
        }

        if (!isApplicationLevelVisible()) {
            return false;
        }

        if (!advancedMode && isVisibleOnlyInAdvanced()) {
            return false;
        }

        if (createInstanceMode && isAdminOnly()) {
            return false;
        }

        if (!managed && isManagedOnly()) {
            return false;
        }

        return true;
    }

}
