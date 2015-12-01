package org.ovirt.engine.core.utils.vdshooks;

import java.util.HashMap;
import java.util.Map;

/**
 * Parsers a hooks string to a  map of script directories/events to a map of script names to a
 * map of script properties.
 * The string is in format of a "toString" invocation on java.util.map object - for
 * example "{before_vm_hibernate={myscript.sh={md5=5d9c4609cd936e80bac8c9ef7b27ea73},
 * 01_log={md5=f388923356e84c2b4149572a44fde2b4}},
 * after_vm_migrate_destination={myscript.sh={md5=677da3bdd8fbd16d4b8917a9fe0f6f89}},
 * after_vm_cont={01_log={md5=f388923356e84c2b4149572a44fde2b4}}}"
 *
 *
 */
public class VdsHooksParser {

    public enum ParsingState {
        START_MAP,
        KEY,
        VALUE
    };

    public static class ParsingResult {
        private Map<String, Object> map;
        private int endIndex;

        public ParsingResult(Map<String, Object> map, int endIndex) {
            this.map = map;
            this.endIndex = endIndex;
        }

        @Override
        public String toString() {
            return map.toString();
        }

        public Map<String, Object> getMap() {
            return map;
        }

        public int getEndIndex() {
            return endIndex;
        }
    }

    protected static ParsingResult parseMap(char[] chars, int startIndex) {
        if (chars[startIndex] != '{') {
            return null;
        }
        int endIndex = -1;
        StringBuilder keyBuilder = new StringBuilder();
        Object value = null;
        Map<String, Object> resultMap = new HashMap<>();
        ParsingState state = ParsingState.START_MAP;
        // Goes over all the characters starting at one position after the opening "{"
        for (int counter = startIndex + 1; counter < chars.length; counter++) {
            char current = chars[counter];
            if (current == ' ') {
                // Ignore spaces
                continue;
            }
            // If a closing "}" was found, break from the loop (in order to return the parsing result
            if (endIndex != -1) {
                break;
            }
            switch (state) {
            case START_MAP:
                // If the character is not "}" - then this is a character of a key in the map,
                // Change to "parsing key" state
                if (current != '}') {
                    state = ParsingState.KEY;
                    keyBuilder = new StringBuilder();
                    handleKey(keyBuilder, current);
                } else {
                    endIndex = counter;
                }
                break;
            case KEY:
                // If the character is = - it means the character is the start of a value which is either a recursive
                // map or a string
                // Switch to "parsing value" state
                if (current == '=') {
                    state = ParsingState.VALUE;
                    if (chars[counter + 1] != '{') {
                        value = new StringBuilder();
                    }
                } else { // Otherwise - continue to parse the key
                    handleKey(keyBuilder, current);
                }
                break;
            case VALUE:
                // If the character is "{" - this means this is the start of a recursive (inner) map
                if (current == '{') {
                    // Parse the inner map, and advance the counter to the end of the map, so its characters will not be
                    // re-parsed
                    value = parseMap(chars, counter);
                    counter = ((ParsingResult) value).getEndIndex();

                } else if (current == ',') {
                    // If the character is "," it means that this is the beginning of a next pair of key and value -
                    // return to "parsing key" state
                    value = putValueInMap(keyBuilder, value, resultMap);
                    keyBuilder = new StringBuilder();
                    state = ParsingState.KEY;
                } else if (current == '}') {
                    // If the character is "}" this is the closing of the map - change the value of endIndex not to be
                    // -1
                    endIndex = counter;
                    value = putValueInMap(keyBuilder, value, resultMap);
                } else {
                    handleValue((StringBuilder) value, current);
                }
                break;

            }
        }
        // At this point, the endIndex is not -1, return the parsing result
        ParsingResult result = new ParsingResult(resultMap, endIndex);
        return result;
    }

    private static Object putValueInMap(StringBuilder keyBuilder, Object value, Map<String, Object> resultMap) {
        if (value instanceof ParsingResult) {
            // Place the map that belongs to the parsing result returned from
            // the recursive call
            value = ((ParsingResult) value).getMap();
        } else {
            // value is StringBuilder, convert it to String
            value = value.toString();
        }
        resultMap.put(keyBuilder.toString(), value);
        return value;
    }

    private static void handleValue(StringBuilder valueBuilder, char current) {
        valueBuilder.append(current);
    }

    private static void handleKey(StringBuilder keyBuilder, char current) {
        keyBuilder.append(current);
    }

    public static Map<String, Object> parseHooks(String hooksStr) {
        char[] chars = new char[hooksStr.length()];
        hooksStr.getChars(0, hooksStr.length(), chars, 0);
        ParsingResult parsingResult = parseMap(chars, 0);
        return parsingResult.getMap();
    }
}
