package org.ovirt.engine.ui.uicommonweb.models.options;

import org.ovirt.engine.core.common.businessentities.UserProfileProperty;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.ui.uicommonweb.models.EntityModel;

/**
 * Abstract field in the {@link EditOptionsModel}
 * @param <T> entity type wrapped by this field
 */
interface Field<T> {
    /** Entity model used by the widgets visible in the UI */
    EntityModel<T> getEntity();

    /**
     * @return true if widget value exists and is different from original value
     */
    boolean isUpdated();

    /**
     * @return true if widget changed to no-value state, but previously it contained a value
     */
    default boolean isRemoved() {
        return false;
    }

    /**
     * @return true if the current widget value is different from original value
     */
    default boolean hasChanged() {
        return isRemoved() || isUpdated();
    }

    /**
     * @return true if Field is backed by a property persisted on the server
     */
    default boolean isOnServer() {
        return !Guid.Empty.equals(toProp().getPropertyId());
    }

    /**
     * Create a new property, based on previous property (if present) but containing the current widgets value
     * @return new property
     */
    UserProfileProperty toProp();

    /**
     * Update state of this field using data from provided property
     * @param prop property supported by this field
     */
    void fromProp(UserProfileProperty prop);

    /**
     * @param prop property to test
     * @return true if the prop is supported by this field
     */
    boolean isSupported(UserProfileProperty prop);

    /**
     * @return true if the field supports resetting
     */
    default boolean isResettable() {
        return false;
    }

    /**
     * @return true if the field contains custom setting (non-default)
     */
    boolean isCustom();

    /**
     * @return translated label for the field (if available), otherwise the property name
     */
    default String getLabel() {
        return getEntity().getTitle() != null ? getEntity().getTitle() : toProp().getName();
    }
}
