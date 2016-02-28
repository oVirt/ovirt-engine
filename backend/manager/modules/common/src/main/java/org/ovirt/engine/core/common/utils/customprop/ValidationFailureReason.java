package org.ovirt.engine.core.common.utils.customprop;

import org.ovirt.engine.core.common.errors.EngineMessage;
import org.ovirt.engine.core.compat.StringFormat;

/**
 * Errors reasons that may appear during custom properties validation
 */
public enum ValidationFailureReason {
    INVALID_DEVICE_TYPE(
            EngineMessage.ACTION_TYPE_FAILED_INVALID_DEVICE_TYPE_FOR_CUSTOM_PROPERTIES,
            "$InvalidDeviceType %1$s"),

    KEY_DOES_NOT_EXIST(EngineMessage.ACTION_TYPE_FAILED_INVALID_CUSTOM_PROPERTIES_INVALID_KEYS, "$MissingKeys %1$s"),

    INCORRECT_VALUE(EngineMessage.ACTION_TYPE_FAILED_INVALID_CUSTOM_PROPERTIES_INVALID_VALUES, "$WrongValueKeys %1$s"),

    SYNTAX_ERROR(EngineMessage.ACTION_TYPE_FAILED_INVALID_CUSTOM_PROPERTIES_INVALID_SYNTAX, ""),

    DUPLICATE_KEY(EngineMessage.ACTION_TYPE_FAILED_INVALID_CUSTOM_PROPERTIES_DUPLICATE_KEYS, "$DuplicateKeys %1$s"),

    NO_ERROR(null, "");

    /**
     * Corresponding global error message
     */
    private final EngineMessage errorMessage;

    /**
     * Global message format string
     */
    private final String messageFormat;

    /**
     * Creates error with specified global error message and message format string
     */
    private ValidationFailureReason(EngineMessage errorMessage, String messageFormat) {
        this.errorMessage = errorMessage;
        this.messageFormat = messageFormat;
    }

    /**
     * Returns corresponding global error message
     */
    public EngineMessage getErrorMessage() {
        return errorMessage;
    }

    /**
     * Formats error message for specified key
     */
    public String formatErrorMessage(String key) {
        if (errorMessage == null) {
            return "";
        } else {
            return StringFormat.format(messageFormat, key);
        }
    }
}
