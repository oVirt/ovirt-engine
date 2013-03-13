package org.ovirt.engine.ui.common.utils;

import java.util.HashMap;
import java.util.Map;
import java.util.MissingResourceException;

import com.google.gwt.i18n.client.Dictionary;

/**
 * Contains dynamic constants available to the application.
 * <p>
 * This class defines all supported constant keys as well as corresponding value accessor methods. Subclasses should
 * register sensible fallback values for supported constant keys.
 */
public class DynamicConstants {

    public enum DynamicConstantKey {

        APPLICATION_TITLE("application_title"), //$NON-NLS-1$
        VERSION_ABOUT("version_about"), //$NON-NLS-1$
        LOGIN_HEADER_LABEL("login_header_label"), //$NON-NLS-1$
        MAIN_HEADER_LABEL("main_header_label"), //$NON-NLS-1$
        COPY_RIGHT_NOTICE("copy_right_notice"), //$NON-NLS-1$
        DOC("doc"); //$NON-NLS-1$

        private final String value;

        DynamicConstantKey(String value) {
            this.value = value;
        }

        public String getValue() {
            return value;
        }

    }

    /**
     * The name under which the dictionary will appear in the host page.
     */
    private static final String MESSAGES_DICTIONARY_NAME = "messages"; //$NON-NLS-1$

    private Dictionary dictionary;

    private final Map<DynamicConstantKey, String> fallbackValues =
            new HashMap<DynamicConstants.DynamicConstantKey, String>();

    public DynamicConstants() {
        try {
            dictionary = Dictionary.getDictionary(MESSAGES_DICTIONARY_NAME);
        } catch (MissingResourceException mre) {
            // Do nothing, the dictionary doesn't exist.
        }
    }

    protected void addFallback(DynamicConstantKey key, String value) {
        fallbackValues.put(key, value);
    }

    protected String getString(DynamicConstantKey key) {
        String fallback = fallbackValues.get(key);
        if (fallback == null) {
            // Use empty string for missing fallback value.
            fallback = ""; //$NON-NLS-1$
        }

        String result = fallback;

        try {
            if (dictionary != null) {
                result = dictionary.get(key.getValue());
            }
        } catch (MissingResourceException mre) {
            // Do nothing, the key doesn't exist.
        }

        return result;
    }

    public final String applicationTitle() {
        return getString(DynamicConstantKey.APPLICATION_TITLE);
    }

    public final String ovirtVersionAbout() {
        return getString(DynamicConstantKey.VERSION_ABOUT);
    }

    public final String loginHeaderLabel() {
        return getString(DynamicConstantKey.LOGIN_HEADER_LABEL);
    }

    public final String mainHeaderLabel() {
        return getString(DynamicConstantKey.MAIN_HEADER_LABEL);
    }

    public final String copyRightNotice() {
        return getString(DynamicConstantKey.COPY_RIGHT_NOTICE);
    }

    public final String engineWebAdminDoc() {
        return getString(DynamicConstantKey.DOC);
    }

}
