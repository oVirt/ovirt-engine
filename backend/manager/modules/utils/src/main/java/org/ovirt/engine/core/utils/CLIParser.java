package org.ovirt.engine.core.utils;

import java.util.HashMap;
import java.util.Set;
import java.util.Vector;

public class CLIParser {
    private Vector<String> argv = new Vector<String>();
    private HashMap<String, String> argsMap = new HashMap<String, String>();
    private int argvIndex = 0;

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

    public String nextParam() {
        String strReturn = null;
        if (argvIndex < argv.size()) {
            strReturn = (String) argv.elementAt(argvIndex++);
        }
        return strReturn;
    }

    public static void main(String[] args) {
        CLIParser parser = new CLIParser(args);
        if (args.length == 0) {
            System.out.println("Usage:\n\t-d or -flag an existance flag that can be checked with hasArg(String) method"
                    + "\n\t-key=value value can be extracted with getArg(String) method");
            System.exit(1);
        }

        System.out.println("arguments sent:\n");
        for (String key : parser.getArgs()) {
            String value = parser.getArg(key);
            System.out.println(" " + key + ": " + value);
        }
    }
}
