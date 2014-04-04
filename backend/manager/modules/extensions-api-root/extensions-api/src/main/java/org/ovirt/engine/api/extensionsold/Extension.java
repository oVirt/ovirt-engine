package org.ovirt.engine.api.extensionsold;

import java.util.Map;

/**
 * Defines the interface for an extension
 */
public interface Extension {

    public static enum ExtensionProperties {
        NAME,
        CONFIGURATION,
        LICENSE,
        PROVIDES,
        VERSION,
        AUTHOR,
        HOME,
        EXTENSION_NAME,
        AAA_CHANGE_EXPIRED_PASSWORD_URL,
        AAA_CHANGE_EXPIRED_PASSWORD_MSG,
        AAA_AUTHENTICATION_CAPABILITIES,
    };

    public static final int AAA_AUTH_CAP_FLAGS_PASSWORD = (1 << 0);
    public static final int AAA_AUTH_CAP_FLAGS_NEGOTIATING = (1 << 1);

    /**
     * Sets the context of the Extension
     *
     * @param context
     */
    void setContext(Map<ExtensionProperties, Object> context);

    /**
     * Gets the context of the Extension
     *
     * @return the extension context
     */
    Map<ExtensionProperties, Object> getContext();

    void init();

}
