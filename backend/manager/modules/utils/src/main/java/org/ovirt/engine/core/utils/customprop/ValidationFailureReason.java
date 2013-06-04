package org.ovirt.engine.core.utils.customprop;

import org.ovirt.engine.core.common.errors.VdcBllMessages;

/**
 * Errors reasons that may appear during custom properties validation
 */
public enum ValidationFailureReason {
    UNSUPPORTED_VERSION(
            VdcBllMessages.ACTION_TYPE_FAILED_CUSTOM_PROPERTIES_NOT_SUPPORTED_IN_VERSION,
            "$NotSupportedInVersion %1$s"),

    INVALID_DEVICE_TYPE(
            VdcBllMessages.ACTION_TYPE_FAILED_INVALID_DEVICE_TYPE_FOR_CUSTOM_PROPERTIES,
            "$InvalidDeviceType %1$s"),

    KEY_DOES_NOT_EXIST(VdcBllMessages.ACTION_TYPE_FAILED_INVALID_CUSTOM_PROPERTIES_INVALID_KEYS, "$MissingKeys %1$s"),

    INCORRECT_VALUE(VdcBllMessages.ACTION_TYPE_FAILED_INVALID_CUSTOM_PROPERTIES_INVALID_VALUES, "$WrongValueKeys %1$s"),

    SYNTAX_ERROR(VdcBllMessages.ACTION_TYPE_FAILED_INVALID_CUSTOM_PROPERTIES_INVALID_SYNTAX, ""),

    DUPLICATE_KEY(VdcBllMessages.ACTION_TYPE_FAILED_INVALID_CUSTOM_PROPERTIES_DUPLICATE_KEYS, "$DuplicateKeys %1$s"),

    NO_ERROR(null, "");

    /**
     * Corresponding global error message
     */
    private final VdcBllMessages errorMessage;

    /**
     * Global message format string
     */
    private final String messageFormat;

    /**
     * Creates error with specified global error message and message format string
     */
    private ValidationFailureReason(VdcBllMessages errorMessage, String messageFormat) {
        this.errorMessage = errorMessage;
        this.messageFormat = messageFormat;
    }

    /**
     * Returns corresponding global error message
     */
    public VdcBllMessages getErrorMessage() {
        return errorMessage;
    }

    /**
     * Formats error message for specified key
     */
    public String formatErrorMessage(String key) {
        if (errorMessage == null) {
            return "";
        } else {
            return String.format(messageFormat, key);
        }
    }
}
