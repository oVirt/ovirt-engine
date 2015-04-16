package org.ovirt.engine.ui.uicommonweb.models.vms;

import org.ovirt.engine.core.compat.Guid;

/**
 * Bean holding two icons in dataUri format.
 * <br/>
 * {@link #icon} - the visible, selected icon
 * <br/>
 * {@link #osDefaultIcon} - icon of currently selected os
 */
public class IconWithOsDefault {

    /**
     * Icon - this value changes over time during model-view communication
     */
    private final String icon;

    /**
     * Default (VM OS based) icon. It allows user to reset custom icon settings.
     */
    private final String osDefaultIcon;

    /**
     * Id of small version of icon downloaded from server. Small icon is not visible, so there is no need to actually
     * download the data.
     */
    private final Guid smallIconId;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        IconWithOsDefault that = (IconWithOsDefault) o;

        if (icon != null ? !icon.equals(that.icon) : that.icon != null) return false;
        if (osDefaultIcon != null ? !osDefaultIcon.equals(that.osDefaultIcon) : that.osDefaultIcon != null)
            return false;
        if (smallIconId != null ? !smallIconId.equals(that.smallIconId) : that.smallIconId != null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = icon != null ? icon.hashCode() : 0;
        result = 31 * result + (osDefaultIcon != null ? osDefaultIcon.hashCode() : 0);
        result = 31 * result + (smallIconId != null ? smallIconId.hashCode() : 0);
        return result;
    }

    public IconWithOsDefault(String icon, String osDefaultIcon, Guid smallIconId) {
        if (icon == null || osDefaultIcon == null) {
            throw new IllegalArgumentException("Both arguments should not be null."); //$NON-NLS-1$
        }
        this.icon = icon;
        this.osDefaultIcon = osDefaultIcon;
        this.smallIconId = smallIconId;
    }

    public String getIcon() {
        return icon;
    }

    public String getOsDefaultIcon() {
        return osDefaultIcon;
    }

    public Guid getSmallIconId() {
        return smallIconId;
    }

    public boolean isCustom() {
        return !getIcon().equals(getOsDefaultIcon());
    }

    @Override
    public String toString() {
        return "IconWithOsDefault{" + //$NON-NLS-1$
                "icon='" + iconUriToString(icon) + //$NON-NLS-1$
                "', osDefaultIcon='" + iconUriToString(osDefaultIcon) + "'" + //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
                ", smallIconId='" + smallIconId + "'" + //$NON-NLS-1$ //$NON-NLS-2$
                '}';
    }

    private static String iconUriToString(String icon) {
        if (icon == null) {
            return "null"; //$NON-NLS-1$
        }
        if (icon.length() <= 30) {
            return icon;
        }
        return icon.substring(0, 30) + "…"; //$NON-NLS-1$
    }
}
