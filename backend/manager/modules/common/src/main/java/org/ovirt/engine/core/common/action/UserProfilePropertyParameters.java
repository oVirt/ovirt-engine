package org.ovirt.engine.core.common.action;

import java.util.Objects;

import org.ovirt.engine.core.common.businessentities.UserProfileProperty;


public class UserProfilePropertyParameters extends ActionParametersBase {
    @SuppressWarnings("FieldMayBeFinal")
    private UserProfileProperty property;

    // required for GWT
    @SuppressWarnings("unused")
    private UserProfilePropertyParameters() {
        property = new UserProfileProperty();
    }

    public UserProfilePropertyParameters(UserProfileProperty property) {
        this.property = Objects.requireNonNull(property, "User profile property cannot be null");
    }

    public UserProfileProperty getUserProfileProperty() {
        return property;
    }
}
