package org.ovirt.engine.ui.webadmin.plugin.api;

import org.ovirt.engine.ui.webadmin.plugin.jsni.JsObjectWithProperties;

import com.google.gwt.core.client.JsArrayString;

/**
 * Represents custom API options object associated with the given UI plugin.
 */
public final class ApiOptions extends JsObjectWithProperties {

    protected ApiOptions() {
    }

    /**
     * Returns allowed origins for which HTML5 {@code message} events should be processed.
     * <p>
     * The value can be either a string (single origin) or a string array (multiple origins).
     * <p>
     * Default return value: empty array (reject all {@code message} events)
     * <p>
     * Example values:
     * <ul>
     * <li>{@code 'http://example.com:8080'} (single origin)
     * <li>{@code ['http://one.com','https://two.org']} (multiple origins)
     * <li>"*" (translates to "any origin", as per HTML5 cross-window messaging specification)
     * </ul>
     */
    public JsArrayString getAllowedMessageOrigins() {
        return getValueAsStringArray("allowedMessageOrigins"); //$NON-NLS-1$
    }

}
