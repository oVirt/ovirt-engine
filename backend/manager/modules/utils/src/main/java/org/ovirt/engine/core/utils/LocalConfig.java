package org.ovirt.engine.core.utils;

import java.io.File;
import java.io.FileReader;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.Reader;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Enumeration;
import java.util.List;
import java.util.Properties;

import org.apache.log4j.Logger;

/**
 * This class stores the local configuration (understanding local as the
 * configuration of the local machine, as oposed to the global configuration
 * stored in the database) of the engine loaded from the file specified by the
 * <code>ENGINE_VARS</code> environment variable.
 */
public class LocalConfig {
    // The log:
    private static final Logger log = Logger.getLogger(LocalConfig.class);

    // Default files for defaults and overriden values:
    private static final String DEFAULTS_PATH = "/usr/share/ovirt-engine/conf/engine.conf.defaults";
    private static final String VARS_PATH = "/etc/ovirt-engine/engine.conf";

    // This is a singleton and this is the instance:
    private static final LocalConfig instance = new LocalConfig();

    public static LocalConfig getInstance() {
        return instance;
    }

    // The properties object storing the current values of the parameters:
    private Properties values = new Properties();

    private LocalConfig() {
        // This is the list of configuration files that will be loaded and
        // merged (the initial size is 2 because usually we will have only two
        // configuration files to merge, the defaults and the variables):
        List<File> configFiles = new ArrayList<File>(2);

        // Locate the defaults file and add it to the list:
        String defaultsPath = System.getenv("ENGINE_DEFAULTS");
        if (defaultsPath == null) {
            defaultsPath = DEFAULTS_PATH;
        }
        File defaultsFile = new File(defaultsPath);
        configFiles.add(defaultsFile);

        // Locate the overriden values file and add it to the list:
        String varsPath = System.getenv("ENGINE_VARS");
        if (varsPath == null) {
            varsPath = VARS_PATH;
        }
        File varsFile = new File(varsPath);
        configFiles.add(varsFile);

        // Locate the override values directory and add the .conf files inside
        // to the list, sorted alphabetically:
        File varsDir = new File(varsPath + ".d");
        if (varsDir.isDirectory()) {
            File[] varsFiles = varsDir.listFiles(
                new FilenameFilter() {
                    @Override
                    public boolean accept(File parent, String name) {
                        return name.endsWith(".conf");
                    }
                }
            );
            Arrays.sort(
                varsFiles,
                new Comparator<File>() {
                    @Override
                    public int compare (File leftFile, File rightFile) {
                        String leftName = leftFile.getName();
                        String rightName = rightFile.getName();
                        return leftName.compareTo(rightName);
                    }
                }
            );
            for (File file : varsFiles) {
                configFiles.add(file);
            }
        }

        // Load the configuration files in the order they are in the list:
        for (File configFile : configFiles) {
            try {
                loadProperties(configFile);
            }
            catch (IOException exception) {
                String message = "Can't load configuration file.";
                log.error(message, exception);
                throw new IllegalStateException(message, exception);
            }
        }

        // Dump the properties to the log (this should probably be DEBUG, but as
        // it will usually happen only once, during the startup, is not that a
        // problem to use INFO):
        @SuppressWarnings("unchecked")
        Enumeration<String> keys = (Enumeration<String>) values.propertyNames();
        while (keys.hasMoreElements()) {
            String key = keys.nextElement();
            String value = values.getProperty(key);
            log.info("Value of property \"" + key + "\" is \"" + value + "\".");
        }
    }

    /**
     * Load the contents of the properties file located by the given environment
     * variable or file.
     *
     * @param file the file that will be used to load the properties if the given
     *    environment variable doesn't have a value
     */
    private void loadProperties(File file) throws IOException {
        // Do nothing if the file doesn't exist or isn't readable:
        if (!file.canRead()) {
            log.warn("The file \"" + file.getAbsolutePath() + "\" doesn't exist or isn't readable. Will return an empty set of properties.");
            return;
        }

        // Load the file:
        Reader reader = null;
        try {
            reader = new FileReader(file);
            values.load(reader);
            log.info("Loaded file \"" + file.getAbsolutePath() + "\".");
        }
        catch (IOException exception) {
            log.error("Can't load file \"" + file.getAbsolutePath() + "\".", exception);
            throw exception;
        }
        finally {
            if (reader != null) {
                try {
                    reader.close();
                }
                catch (IOException exception) {
                    log.error("Can't close file \"" + file.getAbsolutePath() + "\".", exception);
                }
            }
        }
    }

    /**
     * Get the value of a property given its name.
     *
     * @param key the name of the property
     * @return the value of the property as contained in the configuration file
     * @throws java.lang.IllegalStateException if the property doesn't have a
     *     value
     */
    public String getProperty(String key) {
        String value = values.getProperty(key);
        if (value == null) {
            // Loudly alert in the log and throw an exception:
            String message = "The property \"" + key + "\" doesn't have a value.";
            log.error(message);
            throw new IllegalArgumentException(message);

            // Or maybe kill ourselves, as a missing configuration parameter is
            // a serious error:
            // System.exit(1)
        }
        return values.getProperty(key);
    }

    // Accepted values for boolean properties (please keep them sorted as we use
    // a binary search to check if a given property matches one of these
    // values):
    private static final String[] TRUE_VALUES = { "1", "t", "true", "y", "yes" };
    private static final String[] FALSE_VALUES = { "0", "f", "false", "n", "no" };

    /**
     * Get the value of a boolean property given its name. It will take the text
     * of the property and return <true> if it is <code>true</code> if the text
     * of the property is <code>true</code>, <code>t</code>, <code>yes</code>,
     * <code>y</code> or <code>1</code> (ignoring case).
     *
     * @param key the name of the property
     * @return the boolean value of the property
     * @throws java.lang.IllegalArgumentException if the properties doesn't have
     *     a value or if the value is not a valid boolean
     */
    public boolean getBoolean(String key) {
        // Get the text of the property and convert it to lowercase:
        String value = getProperty(key);
        value = value.trim().toLowerCase();

        // Check if it is one of the true values:
        if (Arrays.binarySearch(TRUE_VALUES, value) >= 0) {
            return true;
        }

        // Check if it is one of the false values:
        if (Arrays.binarySearch(FALSE_VALUES, value) >= 0) {
            return false;
        }

        // No luck, will alert in the log that the text is not valid and throw
        // an exception:
        String message = "The value \"" + value + "\" for property \"" + key + "\" is not a valid boolean.";
        log.error(message);
        throw new IllegalArgumentException(message);
    }

    /**
     * Get the value of an integer property given its name. If the text of the
     * value can't be converted to an integer a message will be sent to the log
     * and an exception thrown.
     *
     * @param key the name of the property
     * @return the integer value of the property
     * @throws java.lang.IllegalArgumentException if the property doesn't have a
     *     value or the value is not a valid integer
     */
    public int getInteger(String key) {
        String value = getProperty(key);
        try {
            return Integer.parseInt(value);
        }
        catch (NumberFormatException exception) {
            String message = "The value \"" + value + "\" for property \"" + key + "\" is not a valid integer.";
            log.error(message, exception);
            throw new IllegalArgumentException(message, exception);
        }
    }

    /**
     * Get the value of a property corresponding to a file or directory name.
     *
     * @param key the name of the property
     * @return the file object corresponding to the value of the property
     */
    public File getFile(String key) {
        String value = getProperty(key);
        return new File(value);
    }

    public boolean isProxyEnabled() {
        return getBoolean("ENGINE_PROXY_ENABLED");
    }

    public int getProxyHttpPort() {
        return getInteger("ENGINE_PROXY_HTTP_PORT");
    }

    public int getProxyHttpsPort() {
        return getInteger("ENGINE_PROXY_HTTPS_PORT");
    }

    public String getHost() {
        return getProperty("ENGINE_FQDN");
    }

    public boolean isHttpEnabled() {
        return getBoolean("ENGINE_HTTP_ENABLED");
    }

    public int getHttpPort() {
        return getInteger("ENGINE_HTTP_PORT");
    }

    public boolean isHttpsEnabled() {
        return getBoolean("ENGINE_HTTPS_ENABLED");
    }

    public int getHttpsPort() {
        return getInteger("ENGINE_HTTPS_PORT");
    }

    public File getEtcDir() {
        return getFile("ENGINE_ETC");
    }

    public File getLogDir() {
        return getFile("ENGINE_LOG");
    }

    public File getTmpDir() {
        return getFile("ENGINE_TMP");
    }

    public File getUsrDir() {
        return getFile("ENGINE_USR");
    }

    public File getVarDir() {
        return getFile("ENGINE_VAR");
    }

    public File getLockDir() {
        return getFile("ENGINE_LOCK");
    }

    public File getCacheDir() {
        return getFile("ENGINE_CACHE");
    }

    /**
     * Gets the port number where the engine can be contacted using HTTP from
     * external hosts. This will usually be the proxy HTTP port if the proxy is
     * enabled or the engine HTTP port otherwise.
     */
    public int getExternalHttpPort () {
        return isProxyEnabled()? getProxyHttpPort(): getHttpPort();
    }

    /**
     * Gets the port number where the engine can be contacted using HTTPS from
     * external hosts. This will usually be the proxy HTTPS port if the proxy is
     * enabled or the engine HTTPS port otherwise.
     */
    public int getExternalHttpsPort () {
        return isProxyEnabled()? getProxyHttpsPort(): getHttpsPort();
    }

    /**
     * Gets the URL where the engine can be contacted using HTTP from external
     * hosts. This will usually be the proxy HTTP URL if the proxy is enabled or
     * the engine HTTP URL otherwise.
     *
     * @param path is the path that will be added after the address and port
     *     number of the URL
     */
    public URL getExternalHttpUrl(String path) throws MalformedURLException {
        return new URL("http", getHost(), getExternalHttpPort(), path);
    }

    /**
     * Gets the URL where the engine can be contacted using HTTPS from external
     * hosts. This will usually be the proxy HTTPS URL if the proxy is enabled or
     * the engine HTTPS URL otherwise.
     *
     * @param path is the path that will be added after the address and port
     *     number of the URL
     */
    public URL getExternalHttpsUrl(String path) throws MalformedURLException {
        return new URL("https", getHost(), getExternalHttpsPort(), path);
    }
}
