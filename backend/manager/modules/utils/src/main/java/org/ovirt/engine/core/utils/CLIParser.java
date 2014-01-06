package org.ovirt.engine.core.utils;

import java.util.HashMap;
import java.util.Set;
import java.util.Vector;

public class CLIParser {
    private final Vector<String> argv = new Vector<String>();
    private final HashMap<String, String> argsMap = new HashMap<String, String>();

    public CLIParser(String[] args) {
        for (int i = 0; i < args.length; i++) {
            if (args[i].startsWith("-")) {
                int ix = args[i].indexOf("=");
                String key = (ix > 0) ? args[i].substring(1, ix) : args[i].substring(1);
                String value = (ix > 0) ? args[i].substring(ix + 1) : "";
                argsMap.put(key.toLowerCase(), value);
            } else {
                argv.addElement(args[i]);
            }
        }
    }

    public boolean hasArg(String arg) {
        return argsMap.containsKey(arg.toLowerCase());
    }

    public String getArg(String arg) {
        return (String) argsMap.get(arg.toLowerCase());
    }

    public Set<String> getArgs() {
        return argsMap.keySet();
    }

}
