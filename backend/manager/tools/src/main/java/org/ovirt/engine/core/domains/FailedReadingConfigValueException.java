package org.ovirt.engine.core.domains;

public class FailedReadingConfigValueException extends RuntimeException {
    private static final String ERROR_MESSAGE =
            "Error \"%1$s\" while reading configuration value %2$s.";

    public FailedReadingConfigValueException(String configValue, String error) {
        super(String.format(ERROR_MESSAGE, error, configValue));
    }

}
