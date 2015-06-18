package org.ovirt.engine.core.uutils.cli.parser;

import java.util.Objects;
import java.util.regex.Pattern;

class Argument {

    private String name;
    private String help;
    private String defaultValue;
    private Type type;
    private Pattern matcher;
    private Class<?> valueType;
    private boolean mandatory;
    private String metavar;
    private boolean multivalue;
    private String value;

    enum Type {
        REQUIRED_ARGUMENT,
        OPTIONAL_ARGUMENT,
        NO_ARGUMENT;

        public static Type valueOfIgnoreCase(String name) {
            if(name != null) {
                return valueOf(name.toUpperCase());
            }
            throw new IllegalArgumentException("Invalid value null");
        }
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getHelp() {
        return help;
    }

    public void setHelp(String help) {
        this.help = help;
    }

    public String getDefaultValue() {
        return defaultValue;
    }

    public void setDefaultValue(String defaultValue) {
        this.defaultValue = defaultValue;
    }

    public Type getType() {
        return type;
    }

    public void setType(Type type) {
        this.type = type;
    }

    public Pattern getMatcher() {
        return matcher;
    }

    public void setMatcher(Pattern matcher) {
        this.matcher = matcher;
    }

    public Class<?> getValueType() {
        return valueType;
    }

    public void setValueType(Class<?> valueType) {
        this.valueType = valueType;
    }

    public boolean isMandatory() {
        return mandatory;
    }

    public void setMandatory(boolean mandatory) {
        this.mandatory = mandatory;
    }

    public String getMetavar() {
        return metavar;
    }

    public void setMetavar(String metavar) {
        this.metavar = metavar;
    }

    public boolean isMultivalue() {
        return multivalue;
    }

    public void setMultivalue(boolean multivalue) {
        this.multivalue = multivalue;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    @Override
    public boolean equals(Object obj) {
        if(obj == this) {
            return true;
        }
        if (!(obj instanceof Argument)) {
            return false;
        }

        return Objects.equals(name, ((Argument) obj).getName());
    }

    @Override
    public int hashCode() {
        return Objects.hash(name);
    }
}
