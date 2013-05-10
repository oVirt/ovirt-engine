package org.ovirt.engine.ui.common.widget.uicommon.popup.vm;

public class PopupWidgetConfig {

    private final boolean advancedOnly;

    private final boolean alwaysHidden;

    private final boolean markedAsSpecial;

    private final boolean adminOnly;

    /**
     * The only mutable part - the field can be visible/hidden according to some changing condition in the app
     */
    private boolean applicationLevelVisible = true;

    private PopupWidgetConfig(boolean advancedOnly,
                              boolean alwaysHidden,
                              boolean markedAsSpecial,
                              boolean adminOnly) {
        super();
        this.advancedOnly = advancedOnly;
        this.alwaysHidden = alwaysHidden;
        this.markedAsSpecial = markedAsSpecial;
        this.adminOnly = adminOnly;
    }

    public static PopupWidgetConfig simpleField() {
        return new PopupWidgetConfig(false, false, false, false);
    }

    public static PopupWidgetConfig hiddenField() {
        return new PopupWidgetConfig(false, true, false, false);
    }

    public PopupWidgetConfig withSpecialMark() {
        return new PopupWidgetConfig(advancedOnly, alwaysHidden, true, adminOnly);
    }

    public PopupWidgetConfig visibleInAdvancedModeOnly() {
        return new PopupWidgetConfig(true, alwaysHidden, markedAsSpecial, adminOnly);
    }

    public PopupWidgetConfig visibleForAdminOnly() {
        return new PopupWidgetConfig(advancedOnly, alwaysHidden, markedAsSpecial, true);
    }

    public PopupWidgetConfig copy() {
        PopupWidgetConfig copy = new PopupWidgetConfig(advancedOnly, alwaysHidden, markedAsSpecial, adminOnly);
        copy.setApplicationLevelVisible(isApplicationLevelVisible());
        return copy;
    }

    public boolean isVisibleOnlyInAdvanced() {
        return advancedOnly;
    }

    public boolean isAlwaysHidden() {
        return alwaysHidden;
    }

    public boolean isMarkedAsSpecial() {
        return markedAsSpecial;
    }

    public boolean isAdminOnly() {
        return adminOnly;
    }

    public boolean isApplicationLevelVisible() {
        return applicationLevelVisible;
    }

    public void setApplicationLevelVisible(boolean applicationLevelVisible) {
        this.applicationLevelVisible = applicationLevelVisible;
    }

    public boolean isCurrentlyVisible(boolean advancedMode) {
        if (isAlwaysHidden()) {
            return false;
        }

        if (!isApplicationLevelVisible()) {
            return false;
        }

        if (!advancedMode && isVisibleOnlyInAdvanced()) {
            return false;
        }

        // ignoring the adminOnly flag for now
        return true;
    }

}
