package org.ovirt.engine.core.common.queries;

import org.ovirt.engine.core.common.businessentities.UserProfileProperty;
import org.ovirt.engine.core.compat.Guid;

public class UserProfilePropertyIdQueryParameters extends IdQueryParameters {

    // required for GWT
    @SuppressWarnings("FieldMayBeFinal")
    private UserProfileProperty.PropertyType type;

    // required for GWT
    @SuppressWarnings("unused")
    private UserProfilePropertyIdQueryParameters() {
        this(Guid.Empty, UserProfileProperty.PropertyType.UNKNOWN);
    }

    public UserProfilePropertyIdQueryParameters(Guid id, UserProfileProperty.PropertyType type) {
        super(id);
        this.type = type;
    }

    public UserProfileProperty.PropertyType getType() {
        return type;
    }
}
