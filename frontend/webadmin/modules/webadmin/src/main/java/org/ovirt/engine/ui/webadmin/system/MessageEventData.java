package org.ovirt.engine.ui.webadmin.system;

import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.core.client.JsArrayString;

/**
 * Contains HTML5 {@code message} event data.
 */
public class MessageEventData {

    private static final String ANY_ORIGIN = "*"; //$NON-NLS-1$

    // Origin of the window that sent the message
    private final String origin;

    // Message data, i.e. String or JavaScriptObject instance
    private final Object data;

    // Window that sent the message
    private final JavaScriptObject sourceWindow;

    public MessageEventData(String origin, Object data, JavaScriptObject sourceWindow) {
        this.origin = origin;
        this.data = data;
        this.sourceWindow = sourceWindow;
    }

    /**
     * Returns {@code true} if event origin matches one of {@code allowedSourceOrigins}.
     * <p>
     * Note that "*" translates to "any origin", as per HTML5 cross-window messaging specification.
     */
    public boolean originMatches(JsArrayString allowedSourceOrigins) {
        for (int i = 0; i < allowedSourceOrigins.length(); i++) {
            String allowedOrigin = allowedSourceOrigins.get(i);
            if (allowedOrigin != null && (ANY_ORIGIN.equals(allowedOrigin) || allowedOrigin.equals(origin))) {
                return true;
            }
        }
        return false;
    }

    public String getOrigin() {
        return origin;
    }

    public Object getData() {
        return data;
    }

    public JavaScriptObject getSourceWindow() {
        return sourceWindow;
    }

}
