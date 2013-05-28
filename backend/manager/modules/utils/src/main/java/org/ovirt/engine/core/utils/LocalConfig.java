package org.ovirt.engine.core.utils;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.log4j.Logger;

/**
 * This class stores the local configuration (understanding local as the
 * configuration of the local machine, as opposed to the global configuration
 * stored in the database) of the engine loaded from the file specified by the
 * <code>ENGINE_VARS</code> environment variable.
 */
public class LocalConfig {
    // The log:
    private static final Logger log = Logger.getLogger(LocalConfig.class);

    // Compile regular expressions:
    private static final Pattern EMPTY_LINE = Pattern.compile("^\\s*(#.*)?$");
    private static final Pattern KEY_VALUE_EXPRESSION = Pattern.compile("^\\s*(\\w+)=(.*)$");

    // The properties object storing the current values of the parameters:
    private Map<String, String> values = new HashMap<String, String>();

    protected void loadConfig(String defaultsPath, String varsPath) {
        // Set basic defaults
        values.put("SENSITIVE_KEYS", "");

        // This is the list of configuration files that will be loaded and
        // merged (the initial size is 2 because usually we will have only two
        // configuration files to merge, the defaults and the variables):
        List<File> configFiles = new ArrayList<File>(2);

        File defaultsFile = new File(defaultsPath);
        configFiles.add(defaultsFile);

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
        if (log.isInfoEnabled()) {
            Set<String> keys = values.keySet();
            List<String> list = new ArrayList<String>(keys.size());
            List<String> sensitiveKeys = Arrays.asList(getSensitiveKeys());
            list.addAll(keys);
            Collections.sort(list);
            for (String key : list) {
                String value = "***";
                if (!sensitiveKeys.contains(key)) {
                    value = values.get(key);
                }
                log.info("Value of property \"" + key + "\" is \"" + value + "\".");
            }
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
        BufferedReader reader = null;
        int index = 0;
        try {
            reader = new BufferedReader(new FileReader(file));
            String line = null;
            while ((line = reader.readLine()) != null) {
                index++;
                loadLine(line);
            }
            log.info("Loaded file \"" + file.getAbsolutePath() + "\".");
        }
        catch (Exception e) {
            String msg = String.format(
                "Can't load file '%s' line %d: %s",
                file.getAbsolutePath(),
                index,
                e
            );
            log.error(msg, e);
            throw new RuntimeException(msg, e);
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
     * Expand string using current variables.
     *
     * @return Expanded string.
     * @param value String.
     */
    public String expandString(String value) {
        StringBuilder ret = new StringBuilder();

        boolean escape = false;
        boolean inQuotes = false;
        int index = 0;
        while (index < value.length()) {
            char c = value.charAt(index++);
            if (escape) {
                escape = false;
                ret.append(c);
            }
            else {
                switch(c) {
                    case '\\':
                        escape = true;
                    break;
                    case '$':
                        if (value.charAt(index++) != '{') {
                            throw new RuntimeException("Malformed variable assignement");
                        }
                        int i = value.indexOf('}', index);
                        if (i == -1) {
                            throw new RuntimeException("Malformed variable assignement");
                        }
                        String name = value.substring(index, i);
                        index = i+1;
                        String v = values.get(name);
                        if (v != null) {
                            ret.append(v);
                        }
                    break;
                    case '"':
                        inQuotes = !inQuotes;
                    break;
                    case ' ':
                    case '#':
                        if (inQuotes) {
                            ret.append(c);
                        }
                        else {
                            index = value.length();
                        }
                    break;
                    default:
                        ret.append(c);
                    break;
                }
            }
        }

        return ret.toString();
    }

    /**
     * Load the contents of a line from a properties file, expanding
     * references to variables.
     *
     * @param line the line from the properties file
     */
    private void loadLine(String line) throws IOException {
        Matcher blankMatch = EMPTY_LINE.matcher(line);
        if (!blankMatch.find()) {
            Matcher keyValueMatch = KEY_VALUE_EXPRESSION.matcher(line);
            if (!keyValueMatch.find()) {
                throw new RuntimeException("Invalid line");
            }

            values.put(
                keyValueMatch.group(1),
                expandString(keyValueMatch.group(2))
            );
        }
    }

    /**
     * Get all properties.
     *
     * @return map of all properties.
     */
    public Map<String, String> getProperties() {
        return values;
    }

    /**
     * Get the value of a property given its name.
     *
     * @param key the name of the property
     * @param allowMissing return null if missing
     * @return the value of the property as contained in the configuration file
     * @throws java.lang.IllegalStateException if the property doesn't have a
     *     value
     */
    public String getProperty(String key, boolean allowMissing) {
        String value = values.get(key);
        if (value == null && !allowMissing) {
            // Loudly alert in the log and throw an exception:
            String message = "The property \"" + key + "\" doesn't have a value.";
            log.error(message);
            throw new IllegalArgumentException(message);

            // Or maybe kill ourselves, as a missing configuration parameter is
            // a serious error:
            // System.exit(1)
        }
        return value;
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
        return getProperty(key, false);
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
     * @param defaultValue default value to return, null do not allow.
     * @return the boolean value of the property
     * @throws java.lang.IllegalArgumentException if the properties doesn't have
     *     a value or if the value is not a valid boolean
     */
    public boolean getBoolean(String key, Boolean defaultValue) {
        boolean ret;

        // Get the text of the property and convert it to lowercase:
        String value = getProperty(key, defaultValue != null);
        if (value == null) {
            ret = defaultValue.booleanValue();
        }
        else {
            value = value.trim().toLowerCase();

            // Check if it is one of the true values:
            if (Arrays.binarySearch(TRUE_VALUES, value) >= 0) {
                ret = true;
            }
            // Check if it is one of the false values:
            else if (Arrays.binarySearch(FALSE_VALUES, value) >= 0) {
                ret = false;
            }
            else {
                // No luck, will alert in the log that the text is not valid and throw
                // an exception:
                String message = "The value \"" + value + "\" for property \"" + key + "\" is not a valid boolean.";
                log.error(message);
                throw new IllegalArgumentException(message);
            }
        }

        return ret;
    }

    /**
     * Get the value of a boolean property given its name. It will take the text
     * of the property and return <true> if it is <code>true</code> if the text
     * of the property is <code>true</code>, <code>t</code>, <code>yes</code>,
     * <code>y</code> or <code>1</code> (ignoring case).
     *
     * @param key the name of the property
     * @param defaultValue default value to return, null do not allow.
     * @return the boolean value of the property
     * @throws java.lang.IllegalArgumentException if the properties doesn't have
     *     a value or if the value is not a valid boolean
     */
    public boolean getBoolean(String key) {
        return getBoolean(key, null);
    }

    /**
     * Get the value of an integer property given its name. If the text of the
     * value can't be converted to an integer a message will be sent to the log
     * and an exception thrown.
     *
     * @param key the name of the property
     * @param defaultValue default value to return, null do not allow.
     * @return the integer value of the property
     * @throws java.lang.IllegalArgumentException if the property doesn't have a
     *     value or the value is not a valid integer
     */
    public int getInteger(String key, Integer defaultValue) {
        int ret;

        String value = getProperty(key, defaultValue != null);
        if (value == null) {
            ret = defaultValue.intValue();
        }
        else {
            try {
                ret = Integer.parseInt(value);
            }
            catch (NumberFormatException exception) {
                String message = "The value \"" + value + "\" for property \"" + key + "\" is not a valid integer.";
                log.error(message, exception);
                throw new IllegalArgumentException(message, exception);
            }
        }

        return ret;
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
        return getInteger(key, null);
    }

    /**
     * Get the value of an long property given its name. If the text of the
     * value can't be converted to an long a message will be sent to the log
     * and an exception thrown.
     *
     * @param key the name of the property
     * @param defaultValue default value to return, null do not allow.
     * @return the long value of the property
     * @throws java.lang.IllegalArgumentException if the property doesn't have a
     *     value or the value is not a valid long
     */
    public long getLong(String key, Long defaultValue) {
        long ret;

        String value = getProperty(key, defaultValue != null);
        if (value == null) {
            ret = defaultValue.intValue();
        }
        else {
            try {
                ret = Long.parseLong(value);
            }
            catch (NumberFormatException exception) {
                String message = "The value \"" + value + "\" for property \"" + key + "\" is not a valid long integer.";
                log.error(message, exception);
                throw new IllegalArgumentException(message, exception);
            }
        }

        return ret;
    }

    /**
     * Get the value of an long property given its name. If the text of the
     * value can't be converted to an long a message will be sent to the log
     * and an exception thrown.
     *
     * @param key the name of the property
     * @return the long value of the property
     * @throws java.lang.IllegalArgumentException if the property doesn't have a
     *     value or the value is not a valid long
     */
    public long getLong(String key) {
        return getLong(key, null);
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

    public String[] getSensitiveKeys() {
        return getProperty("SENSITIVE_KEYS").split(",");
    }
}
