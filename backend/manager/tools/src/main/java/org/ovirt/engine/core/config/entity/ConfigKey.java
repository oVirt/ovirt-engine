package org.ovirt.engine.core.config.entity;

import java.security.InvalidParameterException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.builder.ToStringBuilder;
import org.ovirt.engine.core.config.EngineConfigCLIParser;
import org.ovirt.engine.core.config.entity.helper.CompositePasswordValueHelper;
import org.ovirt.engine.core.config.entity.helper.PasswordValueHelper;
import org.ovirt.engine.core.config.entity.helper.ValidationResult;
import org.ovirt.engine.core.config.entity.helper.ValueHelper;

public class ConfigKey {
    private String type;
    private String description;
    private String alternateKey;
    private String keyName;
    private String value;
    private final boolean deprecated;
    private boolean reloadable;
    private List<String> validValues;
    private static final List<String> EMPTY_LIST = new ArrayList<>(0);
    private String version;
    private ValueHelper valueHelper;
    private String defaultValue;

    protected ConfigKey(String type,
            String description,
            String alternateKey,
            String key,
            String value,
            String[] validValues,
            String version,
            ValueHelper helper,
            boolean reloadable,
            boolean deprecated) {
        super();
        this.type = type;
        this.description = description;
        this.alternateKey = alternateKey;
        this.keyName = key;
        this.value = value;
        this.deprecated = deprecated;
        setVersion(version);
        this.validValues = validValues != null ? Arrays.asList(validValues) : EMPTY_LIST;
        this.valueHelper = helper;
        this.reloadable = reloadable;
    }

    public void setVersion(String version) {
        this.version = version == null || version.isEmpty() ? "general" : version;
    }

    public String getType() {
        return type;
    }

    public String getDescription() {
        return description;
    }

    public String getAlternateKeys() {
        return alternateKey;
    }

    public String getKey() {
        return keyName;
    }

    public String getValue() {
        return value;
    }

    public String getDisplayValue() throws Exception {
        return valueHelper.getValue(value);
    }

    public void setType(String type) {
        this.type = type;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void setAlternateKey(String alternateKey) {
        this.alternateKey = alternateKey;
    }

    public void setKey(String key) {
        this.keyName = key;
    }

    /**
     * Sets the value of this Config key to the given value. Is meant to be used before updating the DB, therefore is
     * safe, and throws an Exception in case of validation failure.
     *
     * @param value
     *            The value to set
     */
    public void safeSetValue(String value) throws InvalidParameterException, Exception {
        ValidationResult validationResult = valueHelper.validate(this, value);
        if (!validationResult.isOk()) {
            StringBuilder invalidParamMsg = new StringBuilder();
            invalidParamMsg.append("Cannot set value ")
            .append(value)
            .append(" to key ")
            .append(keyName)
            .append(". ")
            .append(StringUtils.isNotEmpty(validationResult.getDetails()) ? validationResult.getDetails() : "");
            throw new InvalidParameterException(invalidParamMsg.toString());
        }
        this.value = valueHelper.setValue(value);
    }

    public void setParser(EngineConfigCLIParser parser) {
        valueHelper.setParser(parser);
    }

    /**
     * Sets the value of this ConfigKey to the given value without validation. Is meant for internal use only.
     */
    public void unsafeSetValue(String value) {
        this.value = value;
    }

    public List<String> getValidValues() {
        return this.validValues;
    }

    public String getVersion() {
        return version;
    }


    public ValueHelper getValueHelper() {
        return valueHelper;
    }

    public boolean isReloadable() {
        return reloadable;
    }

    public boolean isPasswordKey() {
        return CompositePasswordValueHelper.class.isAssignableFrom(valueHelper.getClass()) ||
                PasswordValueHelper.class.isAssignableFrom(valueHelper.getClass());
    }

    public boolean isDeprecated() {
        return deprecated;
    }

    public String getDefaultValue() {
        return defaultValue;
    }

    public void setDefaultValue(String defaultValue) {
        this.defaultValue = defaultValue;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this)
                .append("type", type)
                .append("description", description)
                .append("alternateKey", alternateKey)
                .append("keyName", keyName)
                .append("value", value)
                .append("deprecated", deprecated)
                .append("reloadable", reloadable)
                .append("validValues", validValues)
                .append("version", version)
                .append("valueHelper", valueHelper)
                .append("defaultValue", defaultValue)
                .toString();
    }
}
