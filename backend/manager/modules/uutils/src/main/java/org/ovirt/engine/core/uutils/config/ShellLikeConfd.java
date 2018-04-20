package org.ovirt.engine.core.uutils.config;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
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

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Shell like configuration file parsing.
 * This class parses a configuration file in a format in which shell uses.
 * It is far from being complete, so caution should be taken. The format which is
 * supported is double quotes, $ to exapnd variables and \ to escape.
 * <p>Examples:</p>
 * {@code}<pre>
 * # comment
 * key00=value0
 * key01=
 * key02=value2
 * key03=value31 value32
 * key04="value41 value42"
 * key05="value51\"value52\"value53"
 * key06="value61#value62"
 * key07="value71#value72"# comment
 * key08="value81#value82" # comment
 * key10="prefix ${key01} ${key02} ${unknown} ${key03} ${key04} suffix"
 * key11="\${key02}"
 * </pre>{@code}
 */
public class ShellLikeConfd {
    private static final Logger log = LoggerFactory.getLogger(ShellLikeConfd.class);

    private static final String SENSITIVE_KEYS = "SENSITIVE_KEYS";

    // Compile regular expressions:
    private static final Pattern EMPTY_LINE = Pattern.compile("^\\s*(#.*)?$");
    private static final Pattern KEY_VALUE_EXPRESSION = Pattern.compile("^\\s*(\\w+)=(.*)$");

    // The properties object storing the current values of the parameters:
    private Map<String, String> values = new HashMap<>();

    /**
     * Use configuration from map.
     * @param values map.
     */
    protected void setConfig(Map<String, String> values) {
        this.values = values;
        dumpConfig();
    }

    /**
     * Use configuration from files.
     * @param defaultsPath path to file containing the defaults.
     * @param varsPath path to file and directory of file.d.
     */
    protected void loadConfig(String defaultsPath, String varsPath) {
        // This is the list of configuration files that will be loaded and
        // merged (the initial size is 2 because usually we will have only two
        // configuration files to merge, the defaults and the variables):
        List<File> configFiles = new ArrayList<>(2);

        if (!StringUtils.isEmpty(defaultsPath)) {
            File defaultsFile = new File(defaultsPath);
            configFiles.add(defaultsFile);
        }

        if (!StringUtils.isEmpty(varsPath)) {
            File varsFile = new File(varsPath);
            configFiles.add(varsFile);

            // Locate the override values directory and add the .conf files inside
            // to the list, sorted alphabetically:
            File[] varsFiles = new File(varsPath + ".d").listFiles((parent, name) -> name.endsWith(".conf"));
            if (varsFiles != null) {
                Arrays.sort(varsFiles, Comparator.comparing(File::getName));
                for (File file : varsFiles) {
                    configFiles.add(file);
                }
            }
        }

        // Load the configuration files in the order they are in the list:
        for (File configFile : configFiles) {
            try {
                loadProperties(configFile);
            } catch (IOException exception) {
                String message = "Can't load configuration file.";
                log.error(message, exception);
                throw new IllegalStateException(message, exception);
            }
        }

        dumpConfig();
    }

    /**
     * Dump all configuration to the log.
     * this should probably be DEBUG, but as it will usually happen only once,
     * during the startup, is not that a roblem to use INFO.
     */
    private void dumpConfig() {
        if (log.isInfoEnabled()) {
            Set<String> keys = values.keySet();
            List<String> list = new ArrayList<>(keys.size());
            List<String> sensitiveKeys = Arrays.asList(getSensitiveKeys());
            list.addAll(keys);
            Collections.sort(list);
            for (String key : list) {
                String value = "***";
                if (!sensitiveKeys.contains(key)) {
                    value = values.get(key);
                }
                log.info("Value of property '{}' is '{}'.", key, value);
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
            log.info("The file '{}' doesn't exist or isn't readable. Will return an empty set of properties.", file.getAbsolutePath());
            return;
        }

        // Load the file:
        int index = 0;
        try (
            BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(file), StandardCharsets.UTF_8))
        ) {
            String line = null;
            while ((line = reader.readLine()) != null) {
                index++;
                loadLine(line);
            }
            log.info("Loaded file '{}'.", file.getAbsolutePath());
        } catch (Exception e) {
            String msg = String.format(
                "Can't load file '%s' line %d: %s",
                file.getAbsolutePath(),
                index,
                e
            );
            log.error(msg, e);
            throw new RuntimeException(msg, e);
        }
    }

    /**
     * Expand string using current variables.
     *
     * @param value String.
     * @return Expanded string.
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
            } else {
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
                        } else {
                            v = System.getProperty(name);
                            if (v != null) {
                                ret.append(v);
                            }
                        }
                    break;
                    case '"':
                        inQuotes = !inQuotes;
                    break;
                    case ' ':
                    case '#':
                        if (inQuotes) {
                            ret.append(c);
                        } else {
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
        return Collections.unmodifiableMap(values);
    }

    /**
     * Get the value of a property given its name and an optional underscore separated suffix.
     * if key_optionalSuffix has a value return it. otherwise return the value of key.
     *
     * @param key            the name of the property
     * @param optionalSuffix the suffix of the property, not including an underscore.
     * @param allowMissing   define the behaviour if both key and key_optionalSuffix are not associated with a value
     * @return the value associated with key_optionalSuffix if it is defined or the one associated with key otherwise.
     * @throws java.lang.IllegalArgumentException
     *      if both key_optionalSuffix and key are not associated with a value and allowMissing is false.
     */
    public String getProperty(String key, String optionalSuffix, boolean allowMissing) {
        String property = getProperty(key + "_" + optionalSuffix, true);
        if (StringUtils.isEmpty(property)) {
            property = getProperty(key, allowMissing);
        }
        return property;
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
     * of the property and return <code>true</code> if it is <code>true</code> if the text
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
        if (StringUtils.isEmpty(value)) {
            ret = defaultValue.booleanValue();
        } else {
            value = value.trim().toLowerCase();

            // Check if it is one of the true values:
            if (Arrays.binarySearch(TRUE_VALUES, value) >= 0) {
                ret = true;
            // Check if it is one of the false values:
            } else if (Arrays.binarySearch(FALSE_VALUES, value) >= 0) {
                ret = false;
            } else {
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
     * of the property and return <code>true</code> if it is <code>true</code> if the text
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
        if (StringUtils.isEmpty(value)) {
            ret = defaultValue.intValue();
        } else {
            try {
                ret = Integer.parseInt(value);
            } catch (NumberFormatException exception) {
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
        if (StringUtils.isEmpty(value)) {
            ret = defaultValue.intValue();
        } else {
            try {
                ret = Long.parseLong(value);
            } catch (NumberFormatException exception) {
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
        String sensitiveKeys = values.get(SENSITIVE_KEYS);
        if (sensitiveKeys == null) {
            return new String[] {};
        } else {
            return sensitiveKeys.split(",");
        }
    }
}
