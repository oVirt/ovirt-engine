package org.ovirt.engine.core.uutils.cli;

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang.StringUtils;

/**
 * Implements argument parser, that supports parsing short and long arguments
 */
public class ExtendedCliParser {
    /**
     * Short argument prefix
     */
    static final String PREFIX_SHORT = "-";

    /**
     * Long argument prefix
     */
    static final String PREFIX_LONG = "--";

    /**
     * Long argument value separator
     */
    static final String VALUE_SEP_LONG = "=";

    /**
     * Map of argument names and their instances
     */
    Map<String, Argument> argMap;

    /**
     * Initializes set of arguments
     */
    public ExtendedCliParser() {
        argMap = new HashMap<>();
    }

    /**
     * Adds argument to parser
     *
     * @param arg
     *            specified argument
     */
    public void addArg(Argument arg) {
        if (StringUtils.isNotBlank(arg.getShortName())
                && argMap.containsKey(arg.getShortName())) {
            throw new IllegalArgumentException(
                    String.format("Argument with short name '%s' already exists!", arg.getShortName()));
        }

        if (StringUtils.isNotBlank(arg.getLongName())
                && argMap.containsKey(arg.getLongName())) {
            throw new IllegalArgumentException(
                    String.format("Argument with long name '%s' already exists!", arg.getLongName()));
        }

        if (arg.getShortName() != null) {
            argMap.put(arg.getShortName(), arg);
        }
        if (arg.getLongName() != null) {
            argMap.put(arg.getLongName(), arg);
        }
    }

    /**
     * Parses arguments in {@code args} and returns map with argument names and their values
     *
     * @param args
     *            array of arguments (usually command line arguments splitted by space)
     * @throws IllegalArgumentException
     *             if {@code args} is {@code null} or arguments has invalid format
     * @return map with argument names and their values
     */
    public Map<String, String> parse(String[] args) {
        if (args == null) {
            throw new IllegalArgumentException("Argumens array cannot be null");
        }

        return parse(args, 0, args.length);
    }

    /**
     * Parses arguments in {@code args} between specified array indexes and returns map with argument names and their
     * values
     *
     * @param args
     *            array of arguments (usually command line arguments splitted by space)
     * @param from
     *            starting index
     * @param to
     *            end index
     * @throws IllegalArgumentException
     *             if {@code args} is {@code null} or arguments has invalid format
     * @throws ArrayIndexOutOfBoundsException
     *             if indexes {@code from} or {@code to} are invalid
     * @return map with argument names and their values
     */
    public Map<String, String> parse(String[] args, int from, int to) {
        if (args == null) {
            throw new IllegalArgumentException("Argumens array cannot be null");
        }

        Map<String, String> result = new HashMap<>();
        // parse arguments
        for (int i = from; i < to;) {
            if (args[i] == null) {
                // just to be sure
                i++;
                continue;
            }

            String name = null;
            String value = null;

            if (isLongArg(args[i])) {
                // parse long argument
                name = parseLongArgName(args[i]);
                value = parseLongArgValue(args[i]);
            } else if (isShortArg(args[i])) {
                // parse short argument
                name = args[i];
                if (i + 1 < args.length) {
                    if (!isShortArg(args[i + 1]) && !isLongArg(args[i + 1])) {
                        // argument has a value
                        value = args[i + 1];
                        i++;
                    }
                }
            } else {
                // invalid argument format
                throw new IllegalArgumentException(
                        String.format("Invalid argument format '%s'!", args[i]));
            }

            Argument arg = argMap.get(name);
            if (arg == null) {
                throw new IllegalArgumentException(
                        String.format("Unknown argument '%s'!", name));
            }
            if (arg.isValueRequied() && StringUtils.isBlank(value)) {
                throw new IllegalArgumentException(
                        String.format("Argument '%s' requires value!", name));
            }
            result.put(arg.getDestination(), value);
            i++;
        }
        return result;
    }

    /**
     * Tests if specified argument contains valid short argument name
     *
     * @param arg
     *            argument
     * @return {@code true} if string contains valid short argument, otherwise {@code false}
     */
    private boolean isShortArg(String arg) {
        boolean result = true;
        if (arg == null || arg.length() != 2) {
            result = false;
        } else if (!arg.startsWith(PREFIX_SHORT)) {
            result = false;
        } else if (!Character.isLetterOrDigit(arg.charAt(1))) {
            result = false;
        }
        return result;
    }

    /**
     * Tests if specified argument contains valid long argument name.
     *
     * @param arg
     *            argument
     * @return {@code true} if string contains valid long argument, otherwise {@code false}
     */
    private boolean isLongArg(String arg) {
        boolean result = true;
        if (arg == null || arg.length() < 3) {
            result = false;
        } else if (!arg.startsWith(PREFIX_LONG)) {
            result = false;
        } else {
            for (int i = 2; i < arg.length(); i++) {
                if (i > 2 && i + 2 < arg.length() && VALUE_SEP_LONG.equals(arg.substring(i, i + 1))) {
                    // argument contains value
                    break;
                }
                char c = arg.charAt(i);
                if (!Character.isLetterOrDigit(c) && c != '-') {
                    result = false;
                    break;
                }
            }
        }
        return result;
    }

    /**
     * Parses name of long argument from specified string. Method DOES NOT validate argument, it just creates
     * substring from start to value separator (or to the end if separator is not present) and removes prefix.
     *
     * @param str
     *            specified string
     * @returns long argument name
     */
    private String parseLongArgName(String str) {
        int idx = str.indexOf(VALUE_SEP_LONG);
        if (idx == -1) {
            // argument does not contain value
            return str;
        } else {
            return str.substring(0, idx);
        }
    }

    /**
     * Parses value of long argument from specified string. Method DOES NOT validate argument, it just creates
     * substring value separator to the end or returns {@code null} if value separator is not present
     *
     * @param str
     *            specified string
     * @returns long argument value or {@code null}
     */
    private String parseLongArgValue(String str) {
        int idx = str.indexOf(VALUE_SEP_LONG);
        if (idx == -1) {
            // arg does not contain value
            return null;
        } else {
            return str.substring(idx + 1);
        }
    }
}
