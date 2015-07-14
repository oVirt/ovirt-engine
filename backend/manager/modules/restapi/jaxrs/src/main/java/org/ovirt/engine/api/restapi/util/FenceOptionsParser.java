package org.ovirt.engine.api.restapi.util;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.ovirt.engine.api.model.Option;
import org.ovirt.engine.api.model.Options;
import org.ovirt.engine.api.model.PowerManagement;

public class FenceOptionsParser {

    /* Format of @str is <agent>;<agent>;...
     * Format of @typeStr is <name>=<type>,<name=type>,...
     */
    public static List<PowerManagement> parse(String str, String typeStr, boolean ignoreValues) {
        List<PowerManagement> ret = new ArrayList<PowerManagement>();

        Map<String, String> types = parseTypes(typeStr);

        for (String agent : str.split(";", -1)) {
            if (!agent.isEmpty()) {
                ret.add(parseAgent(agent, types, ignoreValues));
            }
        }

        return ret;
    }

    public static List<PowerManagement> parse(String str, String typeStr) {
        return parse(str, typeStr, false);
    }

    /* Format is <name>=<bool|int>,<name=bool|int>,...
     */
    private static Map<String, String> parseTypes(String str) {
        Map<String, String> ret = new HashMap<String, String>();

        for (String option : str.split(",", -1)) {
            if (!option.isEmpty()) {
                String[] parts = option.split("=", -1);

                if (parts.length != 2) {
                    throw new IllegalArgumentException("Invalid fencing type description: '" + option + "'");
                }

                ret.put(parts[0], parts[1]);
            }
        }

        return ret;
    }

    /* Format is <agent>:<name=value>,<name=value>,...
     *
     * e.g. alom:secure=secure,port=ipport
     */
    private static PowerManagement parseAgent(String str, Map<String, String> types, boolean ignoreValues) {
        String[] parts = str.split(":", -1);

        if (parts.length != 2) {
            throw new IllegalArgumentException("Invalid fencing agent description: '" + str + "'");
        }

        PowerManagement ret = new PowerManagement();
        ret.setType(parts[0]);
        ret.setOptions(parseOptions(parts[1], types, ignoreValues));
        return ret;
    }

    /* Format is <name=value>,<name=value>,...
     */
    private static Options parseOptions(String str, Map<String, String> types, boolean ignoreValues) {
        Options ret = new Options();

        for (String option : str.split(",", -1)) {
            if (!option.isEmpty()) {
                ret.getOptions().add(parseOption(option, types, ignoreValues));
            }
        }

        return ret;
    }

    /* Format is <name=value>
     */
    private static Option parseOption(String str, Map<String, String> types, boolean ignoreValues) {
        String[] parts = str.split("=", -1);

        if (parts.length != 2) {
            throw new IllegalArgumentException("Invalid fencing option description: '" + str + "'");
        }

        Option ret = new Option();
        ret.setName(parts[0]);

        if (!ignoreValues) {
            ret.setValue(parts[1]);
        }

        if (types.containsKey(parts[0])) {
            ret.setType(types.get(parts[0]));
        } else {
            throw new IllegalArgumentException("No type specified for option: '" + parts[0] + "'");
        }

        return ret;
    }
}
