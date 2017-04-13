package org.ovirt.engine.ui.uicommonweb.models.vms;

import java.util.Objects;

import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.ui.uicommonweb.validation.ValidationResult;

/**
 * Icon model
 */
public class IconWithOsDefault {

    /**
     * Icon - large icon to be used
     */
    private final String icon;

    /**
     * Default (VM OS based) icon. It allows user to reset custom icon settings.
     */
    private final String osDefaultIcon;

    /**
     * Id of small version of icon downloaded from server. Small icon is not visible, so there is no need to actually
     * download the data.
     * <p>
     *     null means that it is unknown i.e. large icon was newly uploaded by user
     * </p>
     */
    private final Guid smallIconId;

    /**
     * Result of validation of the {@link #icon}.
     * <p>
     *     Validation of icons is async operation and UnitVmModel#validate() method works synchronously so the
     *     validation is precomputed.
     * </p>
     * <p>
     *     null indicates that icon was not validated yet.
     * </p>
     */
    private final ValidationResult validationResult;

    public IconWithOsDefault(String icon, String osDefaultIcon, Guid smallIconId, ValidationResult validationResult) {
        if (icon == null || osDefaultIcon == null) {
            throw new IllegalArgumentException("Arguments 'icon' and 'osDefaultIcon' should not be null."); //$NON-NLS-1$
        }
        this.icon = icon;
        this.osDefaultIcon = osDefaultIcon;
        this.smallIconId = smallIconId;
        this.validationResult = validationResult;
    }

    /**
     * This method is intended to be used only with ids of predefined icons.
     */
    public static void create(final Guid largeOsDefaultIconId,
                              final Guid smallOsDefaultIconId,
                              final IconWithOsDefaultCallback callback) {
        IconCache.getInstance().getOrFetchIcon(largeOsDefaultIconId, resolvedIcon -> {
            final IconWithOsDefault instance =
                    new IconWithOsDefault(resolvedIcon, resolvedIcon, smallOsDefaultIconId, ValidationResult.ok());
            callback.onCreated(instance);
        });
    }

    public void withDifferentOsIcon(final Guid osDefaultLargeIconId,
                                    final Guid osDefaultSmallIconId,
                                    final IconWithOsDefaultCallback callback) {
        IconCache.getInstance().getOrFetchIcon(osDefaultLargeIconId, currentOsDefaultIcon -> {
            final IconWithOsDefault newInstance = isCustom()
                    ? new IconWithOsDefault(icon, currentOsDefaultIcon, smallIconId, validationResult)
                    : new IconWithOsDefault(currentOsDefaultIcon, currentOsDefaultIcon, osDefaultSmallIconId,
                            ValidationResult.ok());
            callback.onCreated(newInstance);
        });
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof IconWithOsDefault)) {
            return false;
        }
        IconWithOsDefault other = (IconWithOsDefault) obj;
        return Objects.equals(icon, other.icon)
                && Objects.equals(osDefaultIcon, other.osDefaultIcon)
                && Objects.equals(smallIconId, other.smallIconId)
                && Objects.equals(validationResult, other.validationResult);
    }

    @Override
    public int hashCode() {
        return Objects.hash(
                icon,
                osDefaultIcon,
                smallIconId,
                validationResult
        );
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

    public ValidationResult getValidationResult() {
        return validationResult;
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
                ", validationResult='" + validationResult + "'" + //$NON-NLS-1$ //$NON-NLS-2$
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

    public interface IconWithOsDefaultCallback {
        void onCreated(IconWithOsDefault instance);
    }
}
