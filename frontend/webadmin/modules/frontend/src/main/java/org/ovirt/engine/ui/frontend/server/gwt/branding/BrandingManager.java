package org.ovirt.engine.ui.frontend.server.gwt.branding;

import java.io.File;
import java.io.FileFilter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.regex.Pattern;

import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.node.ObjectNode;
import org.ovirt.engine.core.utils.EngineLocalConfig;

/**
 * This class manages the available branding themes and changeable localized messages.
 */
public class BrandingManager {

    /**
     * The default branding path.
     */
    private static final String BRANDING_PATH = "branding"; //$NON-NLS-1$

    /**
     * The prefix denoting this is part of the branding.
     */
    private static final String BRAND_PREFIX = "obrand"; //$NON-NLS-1$

    /**
     * The prefix used for common messages.
     */
    private static final String COMMON_PREFIX = BRAND_PREFIX + ".common"; //$NON-NLS-1$

    /**
     * The regex pattern to use to determine if a directory should be used
     * as a branding directory. The pattern is '.+\.brand' So anything ending in '.brand' will do.
     */
    private static final Pattern DIRECTORY_PATTERN = Pattern.compile(".+\\.brand"); //$NON-NLS-1$

    /**
     * Only load branding themes for the current branding version. This allows for multiple version of a particular
     * branding theme to exist on the file system without interfering with each other. There is no backwards
     * compatibility, only ONE version will be valid at a time.
     */
    private static final int CURRENT_BRANDING_VERSION = 1;

    /**
     * A list of available {@code BrandingTheme}s.
     */
    private List<BrandingTheme> themes;

    /**
     * The root path of the branding themes.
     */
    private final File brandingRootPath;

    /**
     * Default constructor.
     */
    public BrandingManager() {
        this(EngineLocalConfig.getInstance().getEtcDir());
    }

    /**
     * Constructor that takes a {@code File} object to configure the brandingRootPath.
     * @param etcDir A {@code File} pointing to the branding root path.
     */
    public BrandingManager(final File etcDir) {
        brandingRootPath = new File(etcDir, BRANDING_PATH);
    }

    /**
     * Get a list of available {@code BrandingTheme}s.
     * @return A {@code List} of {@code BrandingTheme}s.
     */
    public synchronized List<BrandingTheme> getBrandingThemes() {
        if (themes == null && brandingRootPath.exists() && brandingRootPath.isDirectory()
                && brandingRootPath.canRead()) {
            themes = new ArrayList<BrandingTheme>();
            List<File> directories = Arrays.asList(
                    brandingRootPath.listFiles(new FileFilter() {
                        @Override
                        public boolean accept(final File file) {
                            return file.isDirectory() && DIRECTORY_PATTERN.matcher(file.getName()).matches();
                        }
                    }));
            Collections.sort(directories);
            for (File directory : directories) {
                BrandingTheme theme = new BrandingTheme(directory.getAbsolutePath(), brandingRootPath,
                        CURRENT_BRANDING_VERSION);
                if (theme.load()) {
                    themes.add(theme);
                }
            }
        }
        return themes;
    }

    /**
     * get a JavaScript associative array string representation of the available messages. Only 'common' messages and
     * messages that have keys that start with the passed in prefix will be returned.
     * @param prefix The prefix to use for getting the keys.
     * @param locale The locale to get the messages for.
     * @return A string of format {'key':'value',...}
     */
    public final String getMessages(final String prefix, final Locale locale) {
        List<BrandingTheme> messageThemes = getBrandingThemes();
        // We need this map to remove potential duplicate strings from the resource bundles.
        Map<String, String> keyValues = new HashMap<String, String>();
        for (BrandingTheme theme : messageThemes) {
            ResourceBundle messagesBundle = theme.getMessagesBundle(locale);
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
        // Turn the map into a string with the format:
        // {"key":"value",...}
        return getMessagesFromMap(keyValues);
    }

    /**
     * @param keyValues The map to turn into the string.
     * @return A string of format {"key":"value",...}
     */
    String getMessagesFromMap(final Map<String, String> keyValues) {
        ObjectNode node = new ObjectMapper().createObjectNode();
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

}
