package org.ovirt.engine.core.searchbackend;

public interface ValueValidationFunction {

    boolean invoke(String field, String value);

}
