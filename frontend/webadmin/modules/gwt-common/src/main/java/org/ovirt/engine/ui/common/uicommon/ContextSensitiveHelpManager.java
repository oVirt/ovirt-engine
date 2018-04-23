package org.ovirt.engine.ui.common.uicommon;

import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.core.client.JsonUtils;
import com.google.gwt.i18n.client.LocaleInfo;
import com.google.gwt.json.client.JSONObject;
import com.google.gwt.json.client.JSONString;

/**
 * Responsible for translating helpTag mappings into context-sensitive help URLs.
 *
 * This class is initialized with a String of json that is read from the server.
 * That json contains helptag-URL mappings for every locale that has the
 * documentation package installed.
 *
 * It parses that json and caches csh mappings for the session. We first try to
 * find csh for the user's locale. If that's not found, we fall back to English.
 *
 * @see ContextSensitiveHelpMappingServlet for the server-side portion of this operation,
 * where the json is generated.
 */
public class ContextSensitiveHelpManager {

    private static final Logger logger = Logger.getLogger(ContextSensitiveHelpManager.class.getName());

    private static final String currentLocale = LocaleInfo.getCurrentLocale().getLocaleName().replaceAll("_", "-"); //$NON-NLS-1$ //$NON-NLS-2$
    private static final String ENGLISH = "en-US"; //$NON-NLS-1$

    private static Map<String, String> cshMappings = null;

    // GWT overlay for the JSON object
    private static class Mapping extends JavaScriptObject {
        @SuppressWarnings("unused")
        protected Mapping() {} // required for GWT
    }

    /**
     * Get the csh path for a helpTag. Called by models / dialogs to see if this dialog has help available.
     *
     * @return URL to csh, or null if this dialog has no csh available.
     */
    public static String getPath(String helpTag) {

        if (cshMappings == null || cshMappings.isEmpty()) {
            // no documentation is installed
            return null;
        }

        if (helpTag != null) {
            return cshMappings.get(helpTag);
        }

        return null;
    }

    /**
     * Get json mappings for the user's current locale, or fall back to English
     * if user's locale is not installed.
     *
     * fileContent is a JSON object with this structure:
     *
     * {
     *     'en-US' : {
     *          'someHelpTag' : '/some/url.html'
     *      },
     *      'ja-JP' : {
     *          'someHelpTag2' : '/some/url2.html'
     *      },
     *      etc.
     * }
     */
    public static void init(String fileContent) {

        Mapping jsonEval = JsonUtils.safeEval(fileContent);
        JSONObject json = new JSONObject(jsonEval);

        Map<String, Map<String, String>> detectedMappings = parseJson(json);

        // all locale's mappings are now in detectedMappings. only need to save the user's locale's mappings, or English.

        if (detectedMappings.isEmpty()) {
            logger.info("No context-sensitive help was found on the server. It will not be available for this session."); //$NON-NLS-1$
            return;
        }

        logger.info("Context-sensitive help is installed on the server. The following locales are available: " //$NON-NLS-1$
                + detectedMappings.keySet());

        if (detectedMappings.keySet().contains(currentLocale)) {
            logger.info("Context-sensitive help for your locale, " + currentLocale + ", is installed and loaded."); //$NON-NLS-1$ //$NON-NLS-2$
            cshMappings = detectedMappings.get(currentLocale);
        } else if (!currentLocale.equals(ENGLISH) && detectedMappings.keySet().contains(ENGLISH)) {
            logger.info("Context-sensitive help wasn't found for your locale, " + currentLocale + ". Using English as a fallback."); //$NON-NLS-1$ //$NON-NLS-2$
            cshMappings = detectedMappings.get(ENGLISH);
        } else {
            logger.info("Context-sensitive help wasn't found for your locale, " + currentLocale + ", or the fallback locale, English. " //$NON-NLS-1$ //$NON-NLS-2$
                    + "Context-sensitive help will not be available for this session."); //$NON-NLS-1$
            cshMappings = null;
        }
    }

    protected static Map<String, Map<String, String>> parseJson(JSONObject json) {

        Map<String, Map<String, String>> detectedMappings = new HashMap<>();

        for (String locale : json.keySet()) {

            JSONObject localeObject = json.get(locale).isObject();
            Map<String, String> localeMappings = new HashMap<>();

            for (String docTag : localeObject.keySet()) {
                JSONString urlString = localeObject.get(docTag).isString();
                if (urlString != null && !docTag.isEmpty() && !urlString.stringValue().isEmpty()
                        && !localeMappings.containsKey(docTag)) {

                    localeMappings.put(docTag, urlString.stringValue());
                }
            }
            // only add the locale to the Map if there were mappings.
            // i.e. a locale with no mappings is treated as if it didn't exist.
            if (!localeMappings.isEmpty()) {
                detectedMappings.put(locale, localeMappings);
            }
        }

        return detectedMappings;
    }
}
