package org.ovirt.engine.core.branding;

import java.io.File;
import java.io.FileFilter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.node.ObjectNode;
import org.ovirt.engine.core.utils.EngineLocalConfig;
import org.ovirt.engine.core.utils.servlet.LocaleFilter;

/**
 * This class manages the available branding themes and changeable localized messages.
 */
public class BrandingManager {
    /**
     * The prefix of the keys in the properties.
     */
    public static final String WELCOME = "welcome";

    /**
     * The default branding path.
     */
    private static final String BRANDING_PATH = "branding"; //$NON-NLS-1$

    /**
     * The prefix denoting this is part of the branding.
     */
    private static final String BRAND_PREFIX = "obrand"; //$NON-NLS-1$

    /**
     * The place holder for the userLocale in the welcome page templates.
     */
    private static final String USER_LOCALE_HOLDER = "\\{userLocale\\}"; //$NON-NLS-1$
    /**
     * The prefix used for common messages.
     */
    private static final String COMMON_PREFIX = BRAND_PREFIX + ".common"; //$NON-NLS-1$

    /**
     * The regular expression {@code Pattern} to use to determine if a directory should be used
     * as a branding directory. The pattern is '.+\.brand' So anything ending in '.brand' will do.
     */
    private static final Pattern DIRECTORY_PATTERN = Pattern.compile(".+\\.brand"); //$NON-NLS-1$

    /**
     * The regular expression {@code Pattern} to use to find the replacement keys.
     */
    private static final Pattern TEMPLATE_PATTERN = Pattern.compile("\\{(\\D[\\w|\\.]*)\\}"); //$NON-NLS-1$

    /**
     * Only load branding themes for the current branding version. This allows for multiple version of a particular
     * branding theme to exist on the file system without interfering with each other. There is no backwards
     * compatibility, only ONE version will be valid at a time.
     */
    private static final int CURRENT_BRANDING_VERSION = 2;

    /**
     * A list of available {@code BrandingTheme}s.
     */
    private List<BrandingTheme> themes;

    /**
     * The root path of the branding themes.
     */
    private final File brandingRootPath;

    /**
     * Instance of the holder pattern, instance doesn't get initialized until needed. This removes
     * the need for synchronization and double locking pattern.
     */
    private static class Holder {
        /**
         * Instance of the BrandingManager.
         */
        static final BrandingManager instance;

        static {
            File etcDir;
            try {
                etcDir = EngineLocalConfig.getInstance().getEtcDir();
            } catch (IllegalArgumentException iae) {
                etcDir = new File(""); // Can't find etcDir, most likely unit tests, pretend there is no branding.
            }
            instance = new BrandingManager(etcDir);
        }
    }

    /**
     * ObjectMapper to translate map into Javascript.
     */
    private final ObjectMapper objectMapper;

    /**
     * Constructor that takes a {@code File} object to configure the brandingRootPath.
     * @param etcDir A {@code File} pointing to the branding root path.
     */
    BrandingManager(final File etcDir) {
        brandingRootPath = new File(etcDir, BRANDING_PATH);
        objectMapper = new ObjectMapper();
    }

    /**
     * Get an instance of the {@code BrandingManager} with the default ETC_DIR.
     * @return A {@code BrandingManager}
     */
    public static BrandingManager getInstance() {
        return Holder.instance;
    }

    /**
     * Get a list of available {@code BrandingTheme}s.
     * @return A {@code List} of {@code BrandingTheme}s.
     */
    public synchronized List<BrandingTheme> getBrandingThemes() {
        if (themes == null && brandingRootPath.exists() && brandingRootPath.isDirectory()
                && brandingRootPath.canRead()) {
            themes = new ArrayList<>();
            File[] directories = brandingRootPath.listFiles(new FileFilter() {
                @Override
                public boolean accept(final File file) {
                    return file.isDirectory() && DIRECTORY_PATTERN.matcher(file.getName()).matches();
                }
            });
            if (directories != null) {
                Arrays.sort(directories);
                for (File directory : directories) {
                    BrandingTheme theme = new BrandingTheme(directory.getAbsolutePath(), brandingRootPath,
                            CURRENT_BRANDING_VERSION);
                    if (theme.load()) {
                        themes.add(theme);
                    }
                }
            }
        }
        return themes != null ? themes : new ArrayList<>();
    }

    /**
     * Get the message associated with the passed in key.
     * @param key The key to get the message for. For instance obrand.common.copy_right_notice.
     * @return The associated message in the default locale.
     */
    public String getMessage(final String key) {
        return getMessage(key, LocaleFilter.DEFAULT_LOCALE);
    }

    /**
     * Get the message associated with the passed in key.
     * @param key The key to get the message for. For instance obrand.common.copy_right_notice.
     * @param locale The locale to use to look up the message.
     * @return The associated message in the passed in locale.
     */
    public String getMessage(final String key, final Locale locale) {
        String result = "";
        // key needs to start with obrand.
        if (key != null && key.startsWith(BRAND_PREFIX + ".")) {
            String[] splitString = key.split("\\.");
            String prefix = (splitString.length >= 2) ? splitString[1] : "";
            if (prefix.length() > 0) {
                result = getMessageMap(prefix, locale).get(key.substring(key.indexOf(prefix) + prefix.length() + 1));
            }
        }
        return result;
    }

    /**
     * Returns a Map of String keys and values.
     * @param prefix The prefix to use for getting the keys.
     * @param locale The locale to get the messages for.
     * @return A {@code Map} of keys and values.
     */
    Map<String, String> getMessageMap(final String prefix, final Locale locale) {
        List<BrandingTheme> messageThemes = getBrandingThemes();
        // We need this map to remove potential duplicate strings from the resource bundles.
        Map<String, String> keyValues = new HashMap<>();
        if (messageThemes != null) {
            for (BrandingTheme theme : messageThemes) {
                List<ResourceBundle> bundles = theme.getMessagesBundle(locale);
                for (ResourceBundle bundle: bundles) {
                    getKeyValuesFromResourceBundle(prefix, keyValues, bundle);
                }
            }
        }
        return keyValues;
    }

    /**
     * Extract values from the passed resource bundle and put it into the passed in Map based on the prefix passed in.
     * @param prefix The prefix to use.
     * @param keyValues The {@code Map} to put the values into.
     * @param messagesBundle The {@code ResourceBundle} to get the values from.
     */
    private void getKeyValuesFromResourceBundle(final String prefix, Map<String, String> keyValues,
            ResourceBundle messagesBundle) {
        for (String key : messagesBundle.keySet()) {
            if (key.startsWith(BRAND_PREFIX + "." + prefix) || key.startsWith(COMMON_PREFIX)) { //$NON-NLS-1$
                // We can potentially override existing values here
                // but this is fine as the themes are sorted in order
                // And later messages should override earlier ones.
                keyValues.put(key.replaceFirst(BRAND_PREFIX + "\\." //$NON-NLS-1$
                        + prefix + "\\.", "") //$NON-NLS-1$
                        .replaceFirst(COMMON_PREFIX + "\\.", ""), //$NON-NLS-1$
                        messagesBundle.getString(key));
            }
        }
    }

    /**
     * get a JavaScript associative array string representation of the available messages. Only 'common' messages and
     * messages that have keys that start with the passed in prefix will be returned.
     * @param prefix The prefix to use for getting the keys.
     * @param locale The locale to get the messages for.
     * @return A string of format {'key':'value',...}
     */
    public String getMessages(final String prefix, final Locale locale) {
        Map<String, String> keyValues = getMessageMap(prefix, locale);
        // Turn the map into a string with the format:
        // {"key":"value",...}
        return getMessagesFromMap(keyValues);
    }
    /**
     * @param keyValues The map to turn into the string.
     * @return A string of format {"key":"value",...}
     */
    String getMessagesFromMap(final Map<String, String> keyValues) {
        ObjectNode node = objectMapper.createObjectNode();
        for (Map.Entry<String, String> entry : keyValues.entrySet()) {
            node.put(entry.getKey(), entry.getValue());
        }
        return node.size() > 0 ? node.toString() : null;
    }

    /**
     * Get the root path of the branding files.
     * @return A {@code File} containing the root path.
     */
    public File getBrandingRootPath() {
        return brandingRootPath;
    }

    /**
     * Look up the welcome section of the top branding theme. The message keys are translated in the language of
     * the passed in {@code Locale}
     * @param locale The {@code Locale} to use to look up the appropriate messages.
     * @return An HTML string to be placed in the welcome page.
     */
    public String getWelcomeSections(final Locale locale) {
        Map<String, String> messageMap = getMessageMap(WELCOME, locale);
        List<BrandingTheme> brandingThemes = getBrandingThemes();
        StringBuilder templateBuilder = new StringBuilder();
        for (BrandingTheme theme: brandingThemes) {
            String template = theme.getWelcomePageSectionTemplate();
            String replacedTemplate = template;
            Matcher keyMatcher = TEMPLATE_PATTERN.matcher(template);
            while (keyMatcher.find()) {
                String key = keyMatcher.group(1);
                // Don't replace {userLocale} here.
                if (!USER_LOCALE_HOLDER.substring(2, USER_LOCALE_HOLDER.length() - 2).equals(key)
                        && messageMap.get(key) != null) {
                    replacedTemplate = replacedTemplate.replaceAll("\\{" + key + "\\}", //$NON-NLS-1 //$NON-NLS-2$
                            messageMap.get(key));
                }
            }
            replacedTemplate = replacedTemplate.replaceAll(USER_LOCALE_HOLDER, locale.toString());
            if (theme.shouldReplaceWelcomePageSectionTemplate()) {
                //Clear the template builder as the theme wants to replace instead of append to the template.
                templateBuilder = new StringBuilder();
            }
            templateBuilder.append(replacedTemplate);
        }
        return templateBuilder.toString();
    }

    /**
     * <p>Look up the path to some cascading-capable resource. Branding uses CSS to handle cascading styles,
     * and a style could be partially overridden by a "higher" brand. But HTML has no way to cascade
     * simple images. So this method implements a similar cascading for other resources, like images
     * (or any other resource that can be served out of a brand).
     * </p>
     * <p>
     * We first look in the highest-numbered theme for the file. If it exists, its path is
     * returned. If that theme has no such file, we look in the next-highest theme. And so on. If no
     * matching files are found, return null.
     * @param resourceName the name of the resource.
     * @return resource to serve, or null if no matching files exist
     */
    public CascadingResource getCascadingResource(final String resourceName) {
        if (resourceName == null) {
            return null;
        }

        List<BrandingTheme> brandingThemes = getBrandingThemes(); // assume these are sorted 00, 01, ...
        // return the first one we find
        for (int i = brandingThemes.size() - 1; i >= 0; i--) {
            CascadingResource cascadingResource = brandingThemes.get(i).getCascadingResource(resourceName);
            if (cascadingResource != null) {
                return cascadingResource;
            }
        }

        // couldn't find it in any brand
        return null;
    }
}
