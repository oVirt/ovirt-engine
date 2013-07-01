package org.ovirt.engine.core.utils.branding;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.IOException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.Locale;
import java.util.Properties;
import java.util.ResourceBundle;

import org.apache.log4j.Logger;
import org.ovirt.engine.core.utils.servlet.LocaleFilter;

/**
 * This class represents the components of an available theme. There are
 * several things that are important to a theme:
 * <ul>
 *  <li>The path to the theme</li>
 *  <li>The name of the style sheet associated with the theme</li>
 * </ul>
 */
public class BrandingTheme {

    /**
     * Enumeration specifying which application type to get a
     * style sheet for.
     */
    public enum ApplicationType {
        /** Web admin application. */
        WEBADMIN("webadmin", "web_admin_css"), //$NON-NLS-1$ //$NON-NLS-2$
        /** User portal application. */
        USER_PORTAL("userportal", "user_portal_css"), //$NON-NLS-1$ //$NON-NLS-2$
        /** Welcome page. */
        WELCOME("welcome", "welcome_css"); //$NON-NLS-1 //$NON-NLS-2$

        /**
         * Prefix associated with application type.
         */
        private final String prefix;
        /**
         * The key to use to get the css file.
         */
        private final String cssKey;

        /**
         * Constructor.
         * @param prefixString The prefix string.
         * @param cssKeyString The key to locate the css file.
         */
        private ApplicationType(final String prefixString, final String cssKeyString) {
            prefix = prefixString;
            cssKey = cssKeyString;
        }

        /**
         * Get the prefix string associated with the application type.
         * @return The prefix {@code String}.
         */
        public String getPrefix() {
            return prefix;
        }

        /**
         * Get the cssKey needed to locale the css file associated with
         * this application type.
         * @return A {@code String} containing the key to locate the css file.
         */
        public String getCssKey() {
            return cssKey;
        }
    }

    /**
     * The logger.
     */
    private static final Logger log = Logger.getLogger(BrandingTheme.class);

    /**
     * The key for the resource bundle name.
     */
    private static final String MESSAGES_KEY = "messages"; //$NON-NLS-1$

    /**
     * The suffix of properties file name.
     */
    private static final String PROPERTIES_FILE_SUFFIX = ".properties"; //$NON-NLS-1$

    /**
     * The name of the branding properties file.
     */
    private static final String BRANDING_PROPERTIES_NAME = "branding" + PROPERTIES_FILE_SUFFIX; //$NON-NLS-1$

    /**
     * The key to use to read the branding version.
     */
    private static final String VERSION_KEY = "version"; //$NON-NLS-1$

    /**
     * The key used to read the welcome page template.
     */
    private static final String TEMPLATE_KEY = "welcome"; //$NON-NLS-1$

    /**
     * The properties associated with the branding theme.
     */
    private final Properties brandingProperties = new Properties();

    /**
     * The path to the branding directory.
     */
    private final String path;

    /**
     * The actual file path.
     */
    private final String filePath;

    /**
     * The version of the branding theme we are supposed to be.
     */
    private final int supportedBrandingVersion;

    /**
     * Availability flag.
     */
    private boolean available;

    /**
     * Constructor.
     * @param brandingPath The path to the theme
     * @param brandingRootPath The root of the path to the branding theme,
     * @param brandingVersion The version to load, if the version don't match the load will fail.
     */
    public BrandingTheme(final String brandingPath, final File brandingRootPath, final int brandingVersion) {
        path = brandingPath.substring(brandingRootPath.getAbsolutePath().length());
        filePath = brandingPath;
        supportedBrandingVersion = brandingVersion;
    }

    /**
     * Load the branding theme based on the passed in paths.
     * @return {@code true} if successfully loaded, {@code false} otherwise.
     */
    public boolean load() {
        final String propertiesFileName = filePath + "/" + BRANDING_PROPERTIES_NAME; //$NON-NLS-1$
        available = false;
        FileInputStream propertiesFile;
        try {
            propertiesFile = new FileInputStream(propertiesFileName);
            brandingProperties.load(propertiesFile);
            available = supportedBrandingVersion == getVersion(brandingProperties);
            if (!available) {
                log.warn("Unable to load branding theme, mismatched version: " //$NON-NLS-1$
                    + getVersion(brandingProperties) + " wanted version: " + supportedBrandingVersion); //$NON-NLS-1$
            }
        } catch (IOException e) {
            // Unable to load properties file, disable theme.
            log.warn("Unable to load properties file for " //$NON-NLS-1$
                    + "theme located here:"//$NON-NLS-1$
                    + propertiesFileName, e);
        }
        return available;
    }

    /**
     * Getter for the style path.
     * @return A {@code String} containing the path.
     */
    public String getPath() {
        return path;
    }

    /**
     * Get the version of the branding read.
     * @param properties The {@code Properties} to use to get the version.
     * @return The version of the branding theme read as an {@code integer}, 0 if we are unable to read
     * the version.
     */
    public int getVersion(final Properties properties) {
        int result = 0;
        try {
            result = Integer.valueOf(properties.getProperty(VERSION_KEY));
        } catch (NumberFormatException nfe) {
            // Do nothing, not a valid version, return 0.
        }
        return result;
    }

    /**
     * Get the style sheet type based on the passed in {@code ApplicationType}.
     * @param type The application type to get the style sheet string for.
     * @return A {@code String} representation of the style sheet name.
     */
    public String getThemeStyleSheet(final ApplicationType type) {
        return brandingProperties.getProperty(type.getCssKey());
    }

    /**
     * Get the theme messages resource bundle for the US locale.
     * @return A {@code ResourceBundle} containing messages.
     */
    public ResourceBundle getMessagesBundle() {
        // Default to US Locale.
        return getMessagesBundle(LocaleFilter.DEFAULT_LOCALE);
    }

    /**
     * Get the theme messages resource bundle.
     * @param locale the locale to load the bundle for.
     * @return A {@code ResourceBundle} containing messages.
     */
    public ResourceBundle getMessagesBundle(final Locale locale) {
        ResourceBundle result = null;
        try {
            File themeDirectory = new File(filePath);
            URLClassLoader urlLoader = new URLClassLoader(
                    new URL[] {
                            themeDirectory.toURI().toURL() });
            final String messageFileName = brandingProperties.getProperty(MESSAGES_KEY);
            String bundleName = messageFileName.lastIndexOf(PROPERTIES_FILE_SUFFIX) != -1
                    ? messageFileName.substring(0, messageFileName.lastIndexOf(PROPERTIES_FILE_SUFFIX))
                    : messageFileName;
            result = ResourceBundle.getBundle(bundleName, locale, urlLoader);
        } catch (IOException e) {
            // Unable to load messages resource bundle.
            log.warn("Unable to read message resource " //$NON-NLS-1$
                    + "bundle, returning null", e); //$NON-NLS-1$
        }
        return result;
    }

    /**
     * Return the raw welcome template as a string.
     * @return The raw template string.
     */
    public String getWelcomePageSectionTemplate() {
        String result = "";
        try {
            final String templateFileName = filePath + "/" + brandingProperties.getProperty(TEMPLATE_KEY); //$NON-NLS-1$
            result = readWelcomeTemplateFile(templateFileName);
        } catch (IOException ioe) {
            log.error("Unable to load welcome template", ioe); //$NON-NLS-1$
        } catch (NullPointerException e) {
            log.error("Unable to locate welcome template key in branding properties", e); //$NON-NLS-1$
        }
        return result;
    }

    /**
     * Read the welcome page template file. The template is standard HTML format, but with one difference.
     * If a line starts with '#' it is considered a comment and will not end up in the output.
     * @param fileName The name of the file to read.
     * @return The contents of the file as a string.
     * @throws IOException if unable to read the template file.
     */
    private String readWelcomeTemplateFile(final String fileName) throws IOException {
        StringBuilder templateBuilder = new StringBuilder();
        BufferedReader bufferedReader = null;

        try {
            String currentLine;
            bufferedReader = new BufferedReader(new FileReader(fileName));

            while ((currentLine = bufferedReader.readLine()) != null) {
                if (!currentLine.startsWith("#")) { // # is comment.
                    templateBuilder.append(currentLine);
                    templateBuilder.append("\n"); //$NON-NLS-1$
                }
            }
        } finally {
            try {
                if (bufferedReader != null) {
                    bufferedReader.close();
                }
            } catch (IOException ex) {
                log.error("Unable to close file reader", ex); //$NON-NLS-1$
                // We have read the entire file, just can't close the reader, return the data.
            }
        }
        return templateBuilder.toString();
    }

    @Override
    public String toString() {
        return "Path to theme: " + getPath() + ", User portal css: " //$NON-NLS-1$ //$NON-NLS-2$
        + getThemeStyleSheet(ApplicationType.USER_PORTAL) + ", Web admin css: " //$NON-NLS-1$
        + getThemeStyleSheet(ApplicationType.WEBADMIN) + ", Welcome page css: " //$NON-NLS-1$
        + getThemeStyleSheet(ApplicationType.WELCOME);
    }

}
