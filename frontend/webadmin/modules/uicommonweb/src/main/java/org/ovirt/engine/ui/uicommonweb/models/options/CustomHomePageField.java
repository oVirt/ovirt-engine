package org.ovirt.engine.ui.uicommonweb.models.options;

import java.util.Objects;

import org.ovirt.engine.core.common.businessentities.UserProfileProperty;
import org.ovirt.engine.ui.uicommonweb.models.EntityModel;

import com.google.gwt.core.client.JsonUtils;
import com.google.gwt.json.client.JSONParser;
import com.google.gwt.json.client.JSONString;

public class CustomHomePageField implements Field<String> {
    private static final String CUSTOM_HOME_PAGE = "webAdmin.customHomePage"; //$NON-NLS-1$
    private final EntityModel<String> customHomePage;
    private final boolean resettable;
    private String originalCustomHomePage;
    private UserProfileProperty prop = UserProfileProperty.builder()
            .withName(CUSTOM_HOME_PAGE)
            .withTypeJson()
            .build();
    private final String defaultValue;

    public CustomHomePageField(EntityModel<String> customHomePage, boolean resettable) {
        this.customHomePage = customHomePage;
        this.defaultValue = customHomePage.getEntity();
        originalCustomHomePage = defaultValue;
        this.resettable = resettable;
    }

    @Override
    public EntityModel<String> getEntity() {
        return customHomePage;
    }

    @Override
    public boolean isUpdated() {
        return !Objects.equals(originalCustomHomePage, getEditedValue())
                && !getEditedValue().isEmpty();
    }

    @Override
    public UserProfileProperty toProp() {
        return UserProfileProperty.builder()
                .from(prop)
                .withContent(encode(getEditedValue()))
                .build();
    }

    private String encode(String value) {
        return JsonUtils.escapeValue(value);
    }

    @Override
    public void fromProp(UserProfileProperty prop) {
        this.prop = prop;
        originalCustomHomePage = parse(prop.getContent());
        customHomePage.setEntity(originalCustomHomePage);
    }

    private String parse(String json) {
        if (json == null || json.isEmpty()) {
            return defaultValue;
        }
        JSONString value = JSONParser.parseStrict(json).isString();
        return value != null ? value.stringValue() : defaultValue;
    }

    @Override
    public boolean isSupported(UserProfileProperty prop) {
        return CUSTOM_HOME_PAGE.equals(prop.getName());
    }

    private String getEditedValue() {
        return customHomePage.getEntity() == null ? defaultValue : customHomePage.getEntity().trim();
    }

    @Override
    public boolean isRemoved() {
        return !Objects.equals(originalCustomHomePage, getEditedValue())
                && getEditedValue().isEmpty();
    }

    @Override
    public boolean isResettable() {
        return resettable;
    }

    @Override
    public boolean isCustom() {
        return !Objects.equals(defaultValue, originalCustomHomePage);
    }
}
