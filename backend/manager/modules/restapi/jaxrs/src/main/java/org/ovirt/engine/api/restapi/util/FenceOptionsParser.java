package org.ovirt.engine.api.restapi.util;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.ovirt.engine.api.model.Agent;
import org.ovirt.engine.api.model.Option;
import org.ovirt.engine.api.model.Options;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class FenceOptionsParser {
    private static Logger log = LoggerFactory.getLogger(FenceOptionsParser.class);

    /* Format of @str is <agent>;<agent>;...
     * Format of @typeStr is <name>=<type>,<name=type>,...
     */
    public static List<Agent> parse(String str, String typeStr, boolean ignoreValues) {
        List<Agent> ret = new ArrayList<>();

        Map<String, String> types = parseTypes(typeStr);

        for (String agent : str.split(";", -1)) {
            if (!agent.isEmpty()) {
                Agent parsedAgent = parseAgent(agent, types, ignoreValues);
                if (parsedAgent != null) {
                    ret.add(parsedAgent);
                }
            }
        }

        return ret;
    }

    public static List<Agent> parse(String str, String typeStr) {
        return parse(str, typeStr, false);
    }

    /* Format is <name>=<bool|int>,<name=bool|int>,...
     */
    private static Map<String, String> parseTypes(String str) {
        Map<String, String> ret = new HashMap<>();

        for (String option : str.split(",", -1)) {
            if (!option.isEmpty()) {
                String[] parts = option.split("=", -1);

                if (parts.length != 2) {
                    log.error("Invalid fencing type description \"{}\".", option);
                    continue;
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
    private static Agent parseAgent(String str, Map<String, String> types, boolean ignoreValues) {
        String[] parts = str.split(":", -1);

        if (parts.length != 2) {
            log.error("Invalid fencing agent description \"{}\".", str);
            return null;
        }

        Agent ret = new Agent();
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
                Option parsedOption = parseOption(option, types, ignoreValues);
                if (parsedOption != null) {
                    ret.getOptions().add(parsedOption);
                }
            }
        }

        return ret;
    }

    /* Format is <name=value>
     */
    private static Option parseOption(String str, Map<String, String> types, boolean ignoreValues) {
        String[] parts = str.split("=", -1);

        if (parts.length != 2) {
            log.error("Invalid fencing option description \"{}\".", str);
            return null;
        }

        Option ret = new Option();
        ret.setName(parts[0]);

        if (!ignoreValues) {
            ret.setValue(parts[1]);
        }

        if (types.containsKey(parts[0])) {
            ret.setType(types.get(parts[0]));
        } else {
            log.error("No type specified for fencing option \"{}\".", parts[0]);
            return null;
        }

        return ret;
    }
}
