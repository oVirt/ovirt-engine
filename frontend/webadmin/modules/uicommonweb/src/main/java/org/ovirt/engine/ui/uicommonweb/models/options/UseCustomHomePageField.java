package org.ovirt.engine.ui.uicommonweb.models.options;

import java.util.Objects;

import org.ovirt.engine.core.common.businessentities.UserProfileProperty;
import org.ovirt.engine.ui.uicommonweb.models.EntityModel;

public class UseCustomHomePageField implements Field<Boolean> {
    private static final String USE_CUSTOM_HOME_PAGE = "webAdmin.useCustomHomePage"; //$NON-NLS-1$
    private final EntityModel<Boolean> isCustom;
    private final boolean resettable;
    private boolean originalIsCustom;
    private UserProfileProperty prop = UserProfileProperty.builder()
            .withName(USE_CUSTOM_HOME_PAGE)
            .withTypeJson()
            .build();
    private Boolean defaultValue;

    public UseCustomHomePageField(EntityModel<Boolean> isCustom, boolean resettable) {
        this.isCustom = isCustom;
        this.resettable = resettable;
        defaultValue = isCustom.getEntity();
        originalIsCustom = defaultValue;
    }

    @Override
    public EntityModel<Boolean> getEntity() {
        return isCustom;
    }

    @Override
    public boolean isUpdated() {
        return !Objects.equals(originalIsCustom, isCustom.getEntity());
    }

    @Override
    public UserProfileProperty toProp() {
        return UserProfileProperty.builder()
                .from(prop)
                .withContent(Boolean.toString(isCustom.getEntity()))
                .build();
    }

    @Override
    public void fromProp(UserProfileProperty prop) {
        this.prop = prop;
        originalIsCustom = Boolean.parseBoolean(prop.getContent());
        isCustom.setEntity(originalIsCustom);
    }

    @Override
    public boolean isSupported(UserProfileProperty prop) {
        return USE_CUSTOM_HOME_PAGE.equals(prop.getName());
    }

    @Override
    public boolean isResettable() {
        return resettable;
    }

    @Override
    public boolean isCustom() {
        return !Objects.equals(defaultValue, originalIsCustom);
    }
}
