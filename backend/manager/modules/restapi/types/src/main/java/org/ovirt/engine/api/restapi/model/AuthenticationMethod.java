package org.ovirt.engine.api.restapi.model;

public enum AuthenticationMethod {
    PASSWORD,
    PUBLICKEY;

    public String value() {
        return name().toLowerCase();
    }

    public static AuthenticationMethod fromValue(String value) {
        try {
            return valueOf(value.toUpperCase());
        } catch (IllegalArgumentException e) {
            return null;
        }
    }

}
