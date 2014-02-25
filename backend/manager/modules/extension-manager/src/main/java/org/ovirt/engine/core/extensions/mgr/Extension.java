package org.ovirt.engine.core.extensions.mgr;

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
        EXTENSION_NAME;
    };

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
