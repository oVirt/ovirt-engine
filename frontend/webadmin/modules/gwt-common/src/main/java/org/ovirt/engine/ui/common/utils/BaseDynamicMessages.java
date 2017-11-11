package org.ovirt.engine.ui.common.utils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.MissingResourceException;
import java.util.Set;

import org.ovirt.engine.ui.frontend.utils.BaseContextPathData;
import org.ovirt.engine.ui.uicommonweb.DynamicMessages;

import com.google.gwt.i18n.client.Dictionary;
import com.google.gwt.i18n.client.LocaleInfo;
import com.google.gwt.regexp.shared.MatchResult;
import com.google.gwt.regexp.shared.RegExp;
import com.google.gwt.safehtml.shared.UriUtils;

/**
 * Contains dynamic messages available to the application.
 * <p>
 * This class defines all supported message keys as well as corresponding value accessor methods. Subclasses should
 * register sensible fallback values for supported message keys.
 */
public class BaseDynamicMessages implements DynamicMessages {

    /**
     * This class defines keys used to look up messages from the {@code Dictionary}.
     */
    public enum DynamicMessageKey {

        APPLICATION_TITLE("application_title"), //$NON-NLS-1$
        COPY_RIGHT_NOTICE("copy_right_notice"), //$NON-NLS-1$
        GUIDE_URL("guide_url"), //$NON-NLS-1$
        GUIDE_LINK_LABEL("guide_link_label"), //$NON-NLS-1$
        CLIENT_RESOURCES("client_resources"), //$NON-NLS-1$
        CONSOLE_CLIENT_RESOURCES("console_client_resources"), //$NON-NLS-1$
        CONSOLE_CLIENT_RESOURCES_URL("console_client_resources_url"), //$NON-NLS-1$
        VENDOR_URL("vendor_url"), //$NON-NLS-1$
        DOC("doc"), //$NON-NLS-1$
        FENCING_OPTIONS_URL("fencing_options_url"); //$NON-NLS-1$

        private final String value;

        DynamicMessageKey(String value) {
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

    /**
     * The pattern used to locate place holders within messages.
     */
    private static final RegExp PLACE_HOLDER_PATTERN = RegExp.compile("\\{(\\d+)\\}", "g"); //$NON-NLS-1$ //$NON-NLS-2$

    /**
     * The {@code Dictionary} that contains the messages from the host page.
     */
    private final Dictionary dictionary;

    /**
     * The {@code Map} containing the fallback values in case the message is not found in the dictionary.
     */
    private final Map<DynamicMessageKey, String> fallbackValues = new HashMap<>();

    public BaseDynamicMessages() {
        this(Dictionary.getDictionary(MESSAGES_DICTIONARY_NAME));
    }

    BaseDynamicMessages(final Dictionary dictionary) {
        this.dictionary = dictionary;
    }

    /**
     * Adds the fallback for a particular key to the fallback map.
     *
     * @param key
     *            The key for the fallback.
     * @param value
     *            The fallback message.
     */
    protected void addFallback(final DynamicMessageKey key, final String value) {
        fallbackValues.put(key, value);
    }

    /**
     * Returns the string value associated with the given key.
     * <p>
     * If the {@code Dictionary} doesn't contain the value associated with given key, then return the fallback value. If
     * the fallback value isn't defined for given key, then return empty string.
     *
     * @param key
     *            The key
     * @return The message, either from the {@code Dictionary} or the fallback value.
     */
    protected String getString(final DynamicMessageKey key) {
        try {
            if (dictionary != null) {
                return dictionary.get(key.getValue());
            }
        } catch (MissingResourceException mre) {
            // Do nothing, the key doesn't exist.
        }

        String fallback = fallbackValues.get(key);
        if (fallback == null) {
            // Use empty string for missing fallback value.
            fallback = ""; //$NON-NLS-1$
        }

        return fallback;
    }

    /**
     * Formats the message associated with the given key using the passed in parameters.
     * <p>
     * The message body must conform to the following standard:
     * <ol>
     * <li>The place holders must follow the following format regex \{\d\}, for instance {0}</li>
     * <li>The place holder sequence must start at 0 and be continuous so {0}, {1}, {2} is valid but {0}, {2} is not</li>
     * <li>One can have the same place holder more than once, so {0}, {0} valid</li>
     * <li>The order is not important, so {2}, {0}, {1} is valid</li>
     * </ol>
     * One can pass more parameters than place holders in the message body, any extra parameters will simply be ignored.
     *
     * @param key
     *            The key to use to lookup the message body.
     * @param args
     *            Zero or more arguments to replace in the message body.
     * @return The formatted string.
     * @throws IllegalArgumentException
     *             if the message body does not conform to the above standard, or if there are less arguments than place
     *             holders in the message body.
     */
    protected String formatString(final DynamicMessageKey key, final String... args) {
        String message = getString(key);
        if (args != null) {
            List<Integer> placeHolderList = getPlaceHolderList(message);
            if (placeHolderList.size() > args.length) {
                throw new IllegalArgumentException("Number of place holders does " //$NON-NLS-1$
                        + "not match number of arguments"); //$NON-NLS-1$
            }
            for (int i = 0; i < args.length; i++) {
                message = message.replaceAll("\\{" + i + "\\}", args[i]); //$NON-NLS-1$ //$NON-NLS-2$
            }
        }
        return message;
    }

    /**
     * Parse the message body and return a list of integers, one for each place holder index in the body.
     * <p>
     * For instance, if the message is 'One {0} over the {1} nest {0}' then the list will be [0,1]. Duplicates are
     * turned into a single element in the resulting list.
     *
     * @param message
     *            The message body to parse.
     * @return A list of integers matching the indexes of the place holders within the message body.
     */
    protected List<Integer> getPlaceHolderList(final String message) {
        MatchResult matcher;
        Set<Integer> matchedPlaceHolders = new HashSet<>();
        for (matcher = PLACE_HOLDER_PATTERN.exec(message); matcher != null;
                matcher = PLACE_HOLDER_PATTERN.exec(message)) {
            matchedPlaceHolders.add(Integer.valueOf(matcher.getGroup(1)));
        }
        List<Integer> result = new ArrayList<>(matchedPlaceHolders);
        Collections.sort(result);
        for (int i = 0; i < result.size(); i++) {
            if (i != result.get(i)) {
                throw new IllegalArgumentException("Invalid place holder index found"); //$NON-NLS-1$
            }
        }
        return result;
    }

    /**
     * Convenience method to get the current locale as a string.
     * @return The current locale as a String.
     */
    protected String getCurrentLocaleAsString() {
        return LocaleInfo.getCurrentLocale().getLocaleName();
    }

    @Override
    public final String applicationTitle() {
        return getString(DynamicMessageKey.APPLICATION_TITLE);
    }

    @Override
    public final String copyRightNotice() {
        return getString(DynamicMessageKey.COPY_RIGHT_NOTICE);
    }

    @Override
    public final String guideUrl() {
        return formatString(DynamicMessageKey.GUIDE_URL, getCurrentLocaleAsString());
    }

    @Override
    public final String guideLinkLabel() {
        return getString(DynamicMessageKey.GUIDE_LINK_LABEL);
    }

    @Override
    public final String consoleClientResources() {
        return getString(DynamicMessageKey.CONSOLE_CLIENT_RESOURCES);
    }

    @Override
    public final String clientResources() {
        return getString(DynamicMessageKey.CLIENT_RESOURCES);
    }

    @Override
    public final String consoleClientResourcesUrl() {
        String url = getString(DynamicMessageKey.CONSOLE_CLIENT_RESOURCES_URL);
        boolean isAbsolute = UriUtils.extractScheme(url) != null;

        return isAbsolute
                 ? url
                 : "/" + BaseContextPathData.getRelativePath() + url; //$NON-NLS-1$
    }

    @Override
    public final String vendorUrl() {
        return getString(DynamicMessageKey.VENDOR_URL);
    }

    @Override
    public final String applicationDocTitle() {
        return getString(DynamicMessageKey.DOC);
    }

    @Override
    public final String fencingOptionsUrl() {
        String url = getString(DynamicMessageKey.FENCING_OPTIONS_URL);
        boolean isAbsolute = UriUtils.extractScheme(url) != null;

        return isAbsolute
                ? url
                : "/" + BaseContextPathData.getRelativePath() + url; //$NON-NLS-1$
    }

}
